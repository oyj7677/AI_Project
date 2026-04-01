#!/usr/bin/env python3

import argparse
import fnmatch
import hashlib
import json
import os
import re
import sys
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime
from typing import Any, Dict, List, Optional, Tuple


INTAKE_MARKER = "<!-- ai-review-intake:bot -->"
INTAKE_DATA_PREFIX = "<!-- ai-review-intake-data:"
MERGE_MARKER = "<!-- safe-auto-merge:bot -->"

LABELS = {
    "ai-review-actionable": {
        "color": "d73a4a",
        "description": "Unresolved actionable AI review feedback exists.",
    },
    "ai-review-manual": {
        "color": "fbca04",
        "description": "Manual review is required due to risk or missing fresh AI review.",
    },
    "safe-auto-merge": {
        "color": "0e8a16",
        "description": "Opt in to merge automatically when all safe gates pass.",
    },
    "safe-auto-merge-blocked": {
        "color": "b60205",
        "description": "Auto merge is requested but currently blocked.",
    },
}

HIGH_RISK_PATTERNS = (
    ".github/workflows/*",
    ".github/actions/*",
    ".github/CODEOWNERS",
    ".github/copilot-instructions.md",
    ".github/agents/*",
    "scripts/*",
    ".env*",
    "AI_TEAM/.env*",
)

LOW_RISK_PATTERNS = (
    "*.md",
    "AI_TEAM/docs/*",
    "AI_TEAM/00_HOME/*",
    ".github/pull_request_template.md",
)

COPILOT_REVIEWERS = (
    "copilot-pull-request-reviewer",
    "copilot-pull-request-reviewer[bot]",
)

BOT_LOGIN_PREFIXES = (
    "copilot-pull-request-reviewer",
    "github-actions",
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="GitHub PR review automation helpers.")
    subparsers = parser.add_subparsers(dest="command", required=True)

    common = argparse.ArgumentParser(add_help=False)
    common.add_argument("--repo", required=True, help="owner/repo")
    common.add_argument("--pr", required=True, type=int, help="Pull request number")

    intake = subparsers.add_parser("intake", parents=[common])
    intake.add_argument("--write", action="store_true", help="Write comment and labels")
    intake.add_argument("--output", help="Optional JSON output path")

    fix_prompt = subparsers.add_parser("fix-prompt", parents=[common])
    fix_prompt.add_argument("--post", action="store_true", help="Post the generated prompt as a PR comment")

    merge = subparsers.add_parser("merge-check", parents=[common])
    merge.add_argument("--write", action="store_true", help="Write comment and labels")
    merge.add_argument("--apply", action="store_true", help="Attempt merge when eligible")
    merge.add_argument(
        "--merge-method",
        default="merge",
        choices=("merge", "squash", "rebase"),
        help="Merge method to use when applying",
    )

    return parser.parse_args()


def token() -> str:
    value = os.environ.get("GH_TOKEN") or os.environ.get("GITHUB_TOKEN")
    if not value:
        raise SystemExit("GH_TOKEN or GITHUB_TOKEN is required")
    return value


def parse_repo(repo: str) -> Tuple[str, str]:
    owner, name = repo.split("/", 1)
    return owner, name


def api_request(method: str, url: str, payload: Optional[dict] = None) -> Any:
    data = None
    if payload is not None:
        data = json.dumps(payload).encode("utf-8")
    request = urllib.request.Request(
        url,
        data=data,
        method=method,
        headers={
            "Authorization": f"Bearer {token()}",
            "Accept": "application/vnd.github+json",
            "Content-Type": "application/json",
            "User-Agent": "ai-project-pr-review-automation",
        },
    )
    try:
        with urllib.request.urlopen(request) as response:
            body = response.read().decode("utf-8")
            return json.loads(body) if body else None
    except urllib.error.HTTPError as exc:
        message = exc.read().decode("utf-8")
        raise RuntimeError(f"{method} {url} failed: {exc.code} {message}") from exc


def graphql(query: str, variables: dict) -> Any:
    return api_request(
        "POST",
        "https://api.github.com/graphql",
        {"query": query, "variables": variables},
    )


def rest(method: str, repo: str, path: str, payload: Optional[dict] = None) -> Any:
    owner, name = parse_repo(repo)
    url = f"https://api.github.com/repos/{owner}/{name}{path}"
    return api_request(method, url, payload)


def is_copilot_author(login: Optional[str]) -> bool:
    if not login:
        return False
    return any(login.startswith(prefix) for prefix in COPILOT_REVIEWERS)


def is_bot_identity(login: Optional[str], name: Optional[str], email: Optional[str]) -> bool:
    if login and any(login.startswith(prefix) for prefix in BOT_LOGIN_PREFIXES):
        return True
    if name and "[bot]" in name.lower():
        return True
    if email and "[bot]" in email.lower():
        return True
    return False


def parse_timestamp(value: Optional[str]) -> Optional[datetime]:
    if not value:
        return None
    return datetime.fromisoformat(value.replace("Z", "+00:00"))


def strip_markdown(text: str) -> str:
    value = re.sub(r"`([^`]*)`", r"\1", text)
    value = re.sub(r"\*\*([^*]+)\*\*", r"\1", value)
    value = re.sub(r"\*([^*]+)\*", r"\1", value)
    value = re.sub(r"\[([^\]]+)\]\([^)]+\)", r"\1", value)
    value = re.sub(r"\s+", " ", value)
    return value.strip()


def sha_id(*parts: str) -> str:
    digest = hashlib.sha1("||".join(parts).encode("utf-8")).hexdigest()
    return digest[:10]


def matches_any(path: str, patterns: Tuple[str, ...]) -> bool:
    return any(fnmatch.fnmatch(path, pattern) for pattern in patterns)


def ensure_label(repo: str, name: str) -> None:
    spec = LABELS[name]
    try:
        rest("POST", repo, "/labels", {"name": name, **spec})
    except RuntimeError as exc:
        if "already_exists" not in str(exc) and '"status":"422"' not in str(exc):
            raise


def add_labels(repo: str, pr_number: int, labels: List[str]) -> None:
    if not labels:
        return
    for label in labels:
        ensure_label(repo, label)
    rest("POST", repo, f"/issues/{pr_number}/labels", {"labels": labels})


def remove_label(repo: str, pr_number: int, label: str) -> None:
    try:
        encoded = urllib.parse.quote(label, safe="")
        rest("DELETE", repo, f"/issues/{pr_number}/labels/{encoded}")
    except RuntimeError as exc:
        if '"status":"404"' not in str(exc):
            raise


def list_issue_comments(repo: str, pr_number: int) -> List[dict]:
    return rest("GET", repo, f"/issues/{pr_number}/comments?per_page=100")


def upsert_issue_comment(repo: str, pr_number: int, marker: str, body: str) -> None:
    comments = list_issue_comments(repo, pr_number)
    existing = next((comment for comment in comments if marker in comment.get("body", "")), None)
    if existing:
        rest("PATCH", repo, f"/issues/comments/{existing['id']}", {"body": body})
        return
    rest("POST", repo, f"/issues/{pr_number}/comments", {"body": body})


def load_pr(repo: str, pr_number: int) -> dict:
    owner, name = parse_repo(repo)
    query = """
    query($owner: String!, $name: String!, $number: Int!) {
      repository(owner: $owner, name: $name) {
        pullRequest(number: $number) {
          number
          title
          url
          state
          isDraft
          mergeStateStatus
          headRefName
          headRefOid
          additions
          deletions
          changedFiles
          labels(first: 100) { nodes { name } }
          files(first: 100) { nodes { path } }
          reviews(first: 100) {
            nodes {
              author { login }
              body
              submittedAt
              state
              url
              commit { oid }
            }
          }
          reviewThreads(first: 100) {
            nodes {
              isResolved
              isOutdated
              path
              comments(first: 20) {
                nodes {
                  author { login }
                  body
                  url
                  createdAt
                  line
                  originalLine
                }
              }
            }
          }
          statusCheckRollup {
            contexts(first: 100) {
              nodes {
                __typename
                ... on CheckRun {
                  name
                  status
                  conclusion
                  detailsUrl
                }
                ... on StatusContext {
                  context
                  state
                  targetUrl
                }
              }
            }
          }
          headRepository {
            nameWithOwner
          }
          latestCommit: commits(last: 1) {
            nodes {
              commit {
                oid
                committedDate
                authors(first: 1) {
                  nodes {
                    name
                    email
                    user {
                      login
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    """
    data = graphql(query, {"owner": owner, "name": name, "number": pr_number})
    repository = data["data"]["repository"]
    if not repository or not repository["pullRequest"]:
        raise SystemExit(f"Pull request #{pr_number} was not found in {repo}")
    pr = repository["pullRequest"]
    pr["issue_comments"] = list_issue_comments(repo, pr_number)
    return pr


def parse_suppressed_feedback(body: str, review_url: str, commit_oid: str) -> List[dict]:
    tasks: List[dict] = []
    details = re.findall(
        r"<summary>Comments suppressed due to low confidence.*?</summary>(.*?)</details>",
        body,
        flags=re.DOTALL,
    )
    for block in details:
        matches = re.findall(r"\*\*(.+?):(\d+)\*\*\s*\n\* (.+?)(?=\n\*\*|\Z)", block, flags=re.DOTALL)
        for path, line, message in matches:
            summary = strip_markdown(message)
            tasks.append(
                {
                    "id": sha_id("suppressed", commit_oid, path, line, summary),
                    "classification": "advisory_low_confidence",
                    "path": path.strip(),
                    "line": int(line),
                    "summary": summary,
                    "detail": summary,
                    "source_url": review_url,
                }
            )
    return tasks


def collect_review_tasks(pr: dict) -> Tuple[List[dict], List[dict], bool]:
    head_oid = pr["headRefOid"]
    actionable: List[dict] = []
    advisory: List[dict] = []
    seen = set()
    fresh_copilot_review = False

    for thread in pr["reviewThreads"]["nodes"]:
        if thread["isResolved"] or thread["isOutdated"]:
            continue
        comments = thread["comments"]["nodes"]
        copilot_comments = [comment for comment in comments if is_copilot_author(comment["author"]["login"])]
        if not copilot_comments:
            continue
        latest = copilot_comments[-1]
        fresh_copilot_review = True
        line = latest.get("line") or latest.get("originalLine") or 0
        summary = strip_markdown(latest["body"])
        key = ("actionable", thread["path"], str(line), summary)
        if key in seen:
            continue
        seen.add(key)
        actionable.append(
            {
                "id": sha_id("thread", head_oid, thread["path"], str(line), summary),
                "classification": "actionable",
                "path": thread["path"],
                "line": int(line) if line else 0,
                "summary": summary,
                "detail": latest["body"].strip(),
                "source_url": latest["url"],
            }
        )

    for review in pr["reviews"]["nodes"]:
        author = review["author"]["login"] if review["author"] else None
        if not is_copilot_author(author):
            continue
        commit_oid = review["commit"]["oid"] if review["commit"] else None
        if commit_oid == head_oid:
            fresh_copilot_review = True
        if commit_oid != head_oid or not review.get("body"):
            continue
        for task in parse_suppressed_feedback(review["body"], review["url"], commit_oid):
            key = (task["classification"], task["path"], str(task["line"]), task["summary"])
            if key in seen:
                continue
            seen.add(key)
            advisory.append(task)

    return actionable, advisory, fresh_copilot_review


def current_labels(pr: dict) -> List[str]:
    return [label["name"] for label in pr["labels"]["nodes"]]


def current_head_reviewed_by_copilot(pr: dict) -> bool:
    head_oid = pr["headRefOid"]
    for review in pr["reviews"]["nodes"]:
        author = review["author"]["login"] if review["author"] else None
        commit_oid = review["commit"]["oid"] if review["commit"] else None
        if is_copilot_author(author) and commit_oid == head_oid:
            return True
    return False


def risk_paths(pr: dict) -> List[str]:
    paths = [node["path"] for node in pr["files"]["nodes"]]
    return [path for path in paths if matches_any(path, HIGH_RISK_PATTERNS)]


def low_risk_only(pr: dict) -> bool:
    paths = [node["path"] for node in pr["files"]["nodes"]]
    return all(matches_any(path, LOW_RISK_PATTERNS) for path in paths)


def docs_only(pr: dict) -> bool:
    paths = [node["path"] for node in pr["files"]["nodes"]]
    return bool(paths) and all(path.endswith(".md") for path in paths)


def latest_commit_meta(pr: dict) -> Dict[str, Any]:
    nodes = pr.get("latestCommit", {}).get("nodes", [])
    if not nodes:
        return {}
    commit = nodes[0]["commit"]
    authors = commit.get("authors", {}).get("nodes", [])
    author = authors[0] if authors else {}
    user = author.get("user") or {}
    return {
        "oid": commit.get("oid"),
        "committed_at": commit.get("committedDate"),
        "author_login": user.get("login"),
        "author_name": author.get("name"),
        "author_email": author.get("email"),
    }


def stale_actionable_tasks(pr: dict) -> List[dict]:
    tasks: List[dict] = []
    seen = set()
    for thread in pr["reviewThreads"]["nodes"]:
        if not thread["isOutdated"]:
            continue
        comments = thread["comments"]["nodes"]
        copilot_comments = [comment for comment in comments if is_copilot_author(comment["author"]["login"])]
        if not copilot_comments:
            continue
        latest = copilot_comments[-1]
        line = latest.get("line") or latest.get("originalLine") or 0
        summary = strip_markdown(latest["body"])
        key = (thread["path"], str(line), summary)
        if key in seen:
            continue
        seen.add(key)
        tasks.append(
            {
                "path": thread["path"],
                "line": int(line) if line else 0,
                "summary": summary,
                "created_at": latest.get("createdAt"),
                "source_url": latest["url"],
            }
        )
    return tasks


def human_followup_ready(pr: dict, summary: dict) -> Tuple[bool, Optional[str]]:
    if summary["fresh_copilot_review"]:
        return False, None
    if summary["actionable_tasks"] or summary["advisory_tasks"] or summary["high_risk_paths"]:
        return False, None
    if not low_risk_only(pr) or not docs_only(pr):
        return False, None

    stale_tasks = stale_actionable_tasks(pr)
    if not stale_tasks:
        return False, None

    latest_commit = latest_commit_meta(pr)
    if not latest_commit:
        return False, None

    if is_bot_identity(
        latest_commit.get("author_login"),
        latest_commit.get("author_name"),
        latest_commit.get("author_email"),
    ):
        return False, None

    latest_commit_time = parse_timestamp(latest_commit.get("committed_at"))
    latest_actionable_time = max(
        (parse_timestamp(task.get("created_at")) for task in stale_tasks if task.get("created_at")),
        default=None,
    )
    if not latest_commit_time or not latest_actionable_time:
        return False, None
    if latest_commit_time <= latest_actionable_time:
        return False, None

    return (
        True,
        "Current head is a human docs-only follow-up commit created after the latest stale Copilot actionable feedback.",
    )


def summarize_status_contexts(pr: dict) -> Tuple[bool, List[str]]:
    failures: List[str] = []
    contexts = pr["statusCheckRollup"]["contexts"]["nodes"] if pr.get("statusCheckRollup") else []
    all_success = True
    for context in contexts:
        if context["__typename"] == "CheckRun":
            name = context["name"]
            status = context.get("status")
            conclusion = context.get("conclusion")
            if status != "COMPLETED" or conclusion not in ("SUCCESS", "SKIPPED", "NEUTRAL"):
                all_success = False
                failures.append(f"{name}: {conclusion or status}")
        else:
            name = context["context"]
            state = context.get("state")
            if state not in ("SUCCESS", "EXPECTED"):
                all_success = False
                failures.append(f"{name}: {state}")
    return all_success, failures


def render_task_line(task: dict) -> str:
    location = task["path"]
    if task.get("line"):
        location += f":{task['line']}"
    return f"- `{location}` {task['summary']}"


def build_intake_summary(pr: dict) -> dict:
    actionable, advisory, fresh = collect_review_tasks(pr)
    high_risk = risk_paths(pr)
    followup_ready, followup_reason = human_followup_ready(
        pr,
        {
            "fresh_copilot_review": fresh,
            "actionable_tasks": actionable,
            "advisory_tasks": advisory,
            "high_risk_paths": high_risk,
        },
    )

    if actionable:
        status = "ACTIONABLE"
        summary = "Actionable AI review feedback is still open."
    elif followup_ready:
        status = "FOLLOWUP_READY"
        summary = "Fresh Copilot review is missing, but this looks like a human follow-up commit that addressed the latest actionable Copilot feedback in a docs-only PR."
    elif high_risk or advisory or not fresh:
        status = "MANUAL_REVIEW"
        summary = "Manual review is still required before merge."
    else:
        status = "CLEAN"
        summary = "No open actionable AI review feedback was detected for the current head commit."

    return {
        "version": 1,
        "status": status,
        "summary": summary,
        "head_oid": pr["headRefOid"],
        "head_ref": pr["headRefName"],
        "fresh_copilot_review": fresh,
        "human_followup_ready": followup_ready,
        "human_followup_reason": followup_reason,
        "high_risk_paths": high_risk,
        "actionable_tasks": actionable,
        "advisory_tasks": advisory,
    }


def intake_comment_body(pr: dict, summary: dict) -> str:
    lines = [
        INTAKE_MARKER,
        "## Review Intake",
        "",
        f"- Status: `{summary['status']}`",
        f"- Head commit: `{summary['head_oid'][:12]}`",
        f"- Fresh Copilot review on current head: `{summary['fresh_copilot_review']}`",
        f"- Human follow-up exception candidate: `{summary.get('human_followup_ready', False)}`",
        f"- Actionable tasks: `{len(summary['actionable_tasks'])}`",
        f"- Advisory tasks: `{len(summary['advisory_tasks'])}`",
        "",
        summary["summary"],
        "",
    ]

    if summary["actionable_tasks"]:
        lines.append("### Actionable Tasks")
        lines.extend(render_task_line(task) for task in summary["actionable_tasks"])
        lines.append("")

    if summary["advisory_tasks"]:
        lines.append("### Advisory Low-Confidence Tasks")
        lines.extend(render_task_line(task) for task in summary["advisory_tasks"])
        lines.append("")

    if summary["high_risk_paths"]:
        lines.append("### High-Risk Paths")
        lines.extend(f"- `{path}`" for path in summary["high_risk_paths"])
        lines.append("")

    if summary.get("human_followup_reason"):
        lines.append("### Follow-up Exception")
        lines.append(f"- {summary['human_followup_reason']}")
        lines.append("")

    lines.extend(
        [
            "### Next Step",
            "- If there are actionable tasks, ask the fix-review-comments agent to address them.",
            "- If only manual review remains, inspect the risky paths and merge deliberately.",
            "- Add the `safe-auto-merge` label only for low-risk PRs that meet the policy.",
            "",
            f"{INTAKE_DATA_PREFIX}{json.dumps(summary, separators=(',', ':'))} -->",
        ]
    )
    return "\n".join(lines)


def sync_intake_labels(repo: str, pr_number: int, summary: dict) -> None:
    if summary["actionable_tasks"]:
        add_labels(repo, pr_number, ["ai-review-actionable"])
    else:
        remove_label(repo, pr_number, "ai-review-actionable")

    if summary["status"] == "MANUAL_REVIEW":
        add_labels(repo, pr_number, ["ai-review-manual"])
    else:
        remove_label(repo, pr_number, "ai-review-manual")


def extract_intake_data(pr: dict) -> Optional[dict]:
    for comment in pr["issue_comments"]:
        body = comment.get("body", "")
        if INTAKE_MARKER not in body:
            continue
        match = re.search(r"<!-- ai-review-intake-data:(.+?) -->", body, flags=re.DOTALL)
        if not match:
            continue
        try:
            return json.loads(match.group(1))
        except json.JSONDecodeError:
            return None
    return None


def build_fix_prompt(pr: dict, summary: dict) -> str:
    tasks = summary["actionable_tasks"]
    if not tasks:
        return (
            "No actionable review-intake tasks were found for the current head commit. "
            "There is nothing to dispatch to the fix-review-comments agent."
        )

    lines = [
        "@copilot Please address the actionable review tasks from the latest review-intake report in this pull request.",
        "",
        "Use the repository guidance in `.github/agents/fix-review-comments.agent.md`.",
        "",
        "Constraints:",
        "- Fix only the actionable tasks listed below.",
        "- Do not change unrelated files.",
        "- Update PR validation or handoff notes if your change affects them.",
        "- Push commits to this pull request branch.",
        "- Do not merge the pull request.",
        "",
        "Actionable tasks:",
    ]
    lines.extend(render_task_line(task) for task in tasks)
    return "\n".join(lines)


def merge_comment_body(pr: dict, evaluation: dict) -> str:
    lines = [
        MERGE_MARKER,
        "## Safe Auto Merge",
        "",
        f"- Eligible: `{evaluation['eligible']}`",
        f"- Requested: `{evaluation['requested']}`",
        f"- Human follow-up exception used: `{evaluation.get('human_followup_exception_used', False)}`",
        f"- Head commit: `{pr['headRefOid'][:12]}`",
        "",
    ]
    if evaluation["reasons"]:
        lines.append("### Blocking Reasons")
        lines.extend(f"- {reason}" for reason in evaluation["reasons"])
        lines.append("")
    else:
        lines.extend(
            [
                "### Result",
                "- All safe auto-merge gates passed for this pull request.",
                "",
            ]
        )
    return "\n".join(lines)


def evaluate_safe_merge(repo: str, pr: dict, summary: dict) -> dict:
    labels = set(current_labels(pr))
    checks_ok, failures = summarize_status_contexts(pr)
    requested = "safe-auto-merge" in labels
    reasons: List[str] = []
    followup_exception_used = bool(summary.get("human_followup_ready"))

    if not requested:
        reasons.append("`safe-auto-merge` label is not present.")
    if pr["state"] != "OPEN":
        reasons.append("Pull request is not open.")
    if pr["isDraft"]:
        reasons.append("Pull request is still draft.")
    if pr["mergeStateStatus"] != "CLEAN":
        reasons.append(f"Merge state is `{pr['mergeStateStatus']}`.")
    if not checks_ok:
        reasons.append("Required checks are not green: " + ", ".join(failures))
    if summary["head_oid"] != pr["headRefOid"]:
        reasons.append("Review intake report is stale for the current head commit.")
    if not summary["fresh_copilot_review"] and not followup_exception_used:
        reasons.append("No fresh Copilot review exists for the current head commit.")
    if summary["actionable_tasks"]:
        reasons.append("Actionable AI review tasks are still open.")
    if summary["high_risk_paths"]:
        reasons.append("High-risk paths were changed: " + ", ".join(summary["high_risk_paths"]))
    if not low_risk_only(pr):
        reasons.append("Changed files are outside the low-risk auto-merge allowlist.")
    if pr["changedFiles"] > 10:
        reasons.append("PR changes too many files for safe auto-merge.")
    if pr["additions"] + pr["deletions"] > 400:
        reasons.append("PR diff is larger than the safe auto-merge threshold.")
    head_repo = pr["headRepository"]["nameWithOwner"] if pr.get("headRepository") else ""
    if head_repo and head_repo != repo:
        reasons.append("Head branch is from a forked repository.")

    return {
        "requested": requested,
        "eligible": not reasons,
        "reasons": reasons,
        "human_followup_exception_used": followup_exception_used,
    }


def apply_safe_merge(repo: str, pr_number: int, merge_method: str) -> str:
    try:
        response = rest(
            "PUT",
            repo,
            f"/pulls/{pr_number}/merge",
            {"merge_method": merge_method},
        )
    except RuntimeError as exc:
        message = str(exc)
        if "Merge already in progress" in message:
            return "merge_in_progress"
        raise
    if response and response.get("merged"):
        return "merged"
    return "noop"


def handle_intake(args: argparse.Namespace) -> int:
    pr = load_pr(args.repo, args.pr)
    summary = build_intake_summary(pr)
    body = intake_comment_body(pr, summary)

    if args.output:
        with open(args.output, "w", encoding="utf-8") as file:
            json.dump(summary, file, indent=2)

    if args.write:
        upsert_issue_comment(args.repo, args.pr, INTAKE_MARKER, body)
        sync_intake_labels(args.repo, args.pr, summary)
    else:
        print(body)
    return 0


def handle_fix_prompt(args: argparse.Namespace) -> int:
    pr = load_pr(args.repo, args.pr)
    summary = build_intake_summary(pr)
    body = build_fix_prompt(pr, summary)
    if args.post:
        rest("POST", args.repo, f"/issues/{args.pr}/comments", {"body": body})
    else:
        print(body)
    return 0


def handle_merge_check(args: argparse.Namespace) -> int:
    pr = load_pr(args.repo, args.pr)
    summary = build_intake_summary(pr)
    evaluation = evaluate_safe_merge(args.repo, pr, summary)
    body = merge_comment_body(pr, evaluation)

    if args.write:
        upsert_issue_comment(args.repo, args.pr, MERGE_MARKER, body)
        if evaluation["requested"] and not evaluation["eligible"]:
            add_labels(args.repo, args.pr, ["safe-auto-merge-blocked"])
        else:
            remove_label(args.repo, args.pr, "safe-auto-merge-blocked")
    else:
        print(body)

    if args.apply and evaluation["eligible"]:
        result = apply_safe_merge(args.repo, args.pr, args.merge_method)
        print(json.dumps({"result": result}))
    return 0


def main() -> int:
    args = parse_args()
    if args.command == "intake":
        return handle_intake(args)
    if args.command == "fix-prompt":
        return handle_fix_prompt(args)
    if args.command == "merge-check":
        return handle_merge_check(args)
    raise SystemExit(f"Unknown command: {args.command}")


if __name__ == "__main__":
    sys.exit(main())

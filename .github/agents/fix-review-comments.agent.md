---
name: Fix Review Comments
description: Read the latest review-intake tasks, implement only the actionable feedback, validate the change, and push an update without merging.
---

# Fix Review Comments

You are the repair lane after review-intake has produced actionable tasks.

## Mission

Resolve only the actionable review tasks for the current pull request, keep the patch minimal, and leave the branch ready for re-review.

## Required Inputs

- The latest `Review Intake` comment on the pull request
- The current pull request diff
- The repository policy in `.github/copilot-instructions.md`

## Workflow

1. Read the hidden intake data or the task list from the latest `Review Intake` comment.
2. Ignore `advisory_low_confidence` items unless the fix is obvious and low risk.
3. Change only the files needed to address the actionable items.
4. Run the smallest meaningful validation for the touched area.
5. Update PR notes if the validation story changed.
6. Commit and push to the current PR branch.
7. Do not merge the pull request.

## Repository-Specific Rules

- If the task touches `.github/workflows/**`, security-sensitive automation, or merge policy, be conservative and avoid broad refactors.
- If no actionable tasks exist, do not churn the branch.
- If you believe a Copilot comment is incorrect, explain that in a PR comment instead of forcing a code change.
- Leave the branch in a state where `safe-auto-merge` can evaluate it again.

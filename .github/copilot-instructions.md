When reviewing pull requests in this repository, act as the independent reviewer
for a solo maintainer.

- Prioritize correctness, regressions, missing validation, workflow safety,
  permission mistakes, and secret exposure over style nits.
- Read the PR template sections `Validation`, `Risks`, `Review Notes`, and
  `AI Review Handoff` before judging merge readiness.
- For `.github/**` changes, focus on token scope, event choice, branch safety,
  accidental write permissions, and whether a workflow can be abused from forks.
- For `AI_TEAM/docs/**` changes, check that policy text matches the repository's
  actual GitHub settings and automation behavior.
- Keep comments concise and actionable. Prefer a small number of high-signal
  findings over broad summaries.
- End the review with a merge recommendation using one of:
  `Ready`, `Ready with follow-ups`, `Not ready`.
- Do not assume a human co-reviewer exists. If something would normally require a
  second human reviewer, call it out explicitly so the maintainer can self-check
  before merging.

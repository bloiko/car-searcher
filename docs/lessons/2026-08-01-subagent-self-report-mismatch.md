# Subagent self-reports about "who changed what" were unreliable, even though the code was correct

**Task:** car-photos, task #1 (`Car.photoUrls` + `CarIndexMapping`).

**What happened:** test-author's report claimed it added only a failing test plus a non-implementing stub (a temporary 7-arg constructor, explicitly *not* adding `photoUrls` validation — "this is intentionally the RED gap"). The implementer's report then claimed `Car.java` and two test files were "already" fully correct when it started, that no stub existed, and that it made no changes to them.

`git diff --stat` against the last commit showed all of those files genuinely modified in this session. At least one report was simply wrong about what happened — and there was no way to tell from the narratives alone which one, or whether real validation logic even existed at all versus a half-finished stub someone rubber-stamped.

**Evidence, not narrative, resolved it:** the reviewer ignored both reports and read the actual diff content. The validation logic in `Car.java` was complete and coherent — no leftover stub, no seam suggesting an unfinished swap. An independent `mvn clean verify` passed clean, and the one questionable claim (a new SpotBugs exclusion) was stress-tested by temporarily removing it, confirming the exact single finding it claimed to suppress, then restoring it byte-for-byte.

**Why this matters:** this is the entire reason review re-runs the gate independently instead of trusting either prior pass's account of its own work. It worked exactly as designed here — the code was fine, only the self-reporting was sloppy. But it's a reminder that subagent reports about *what they personally did* (as opposed to what's true on disk) are not reliable evidence, even absent any incentive to misrepresent — treat "I found X already there" or "I only did Y" claims as unverified until the diff itself confirms them.

**Tag:** `subagent-self-report`, `process`

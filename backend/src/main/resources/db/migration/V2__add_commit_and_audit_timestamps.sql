-- Adds commit linkage to tables that the History page needs to join by
-- commit_sha, and a real timestamp to access_audit so it can be ordered.
-- All new columns are nullable: existing rows have no commit/timestamp to
-- backfill, and reads treat a null commit_sha as "not attributable to a
-- specific commit" rather than inventing one.

ALTER TABLE validation_results ADD COLUMN commit_sha VARCHAR(64) NULL;
ALTER TABLE drift_findings ADD COLUMN commit_sha VARCHAR(64) NULL;
ALTER TABLE rotation_events ADD COLUMN commit_sha VARCHAR(64) NULL;
ALTER TABLE access_audit ADD COLUMN captured_at TIMESTAMP NULL;

CREATE INDEX idx_validation_results_commit_sha ON validation_results (commit_sha);
CREATE INDEX idx_drift_findings_commit_sha ON drift_findings (commit_sha);
CREATE INDEX idx_rotation_events_commit_sha ON rotation_events (commit_sha);

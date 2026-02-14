-- V1__spring_batch_postgres.sql
-- Single-file Spring Batch schema for PostgreSQL (tables + sequences)
-- Drops any leftover MySQL-style seq tables, creates tables, then sequences.

-- ------------------------------------------------------------------
-- Remove old MySQL-style sequence tables if they exist (safe to run)
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS batch_job_instance_seq;
DROP TABLE IF EXISTS batch_job_execution_seq;
DROP TABLE IF EXISTS batch_step_execution_seq;

-- ------------------------------------------------------------------
-- Core tables
-- ------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS batch_job_instance (
    job_instance_id BIGINT NOT NULL PRIMARY KEY,
    version BIGINT,
    job_name VARCHAR(100) NOT NULL,
    job_key VARCHAR(32) NOT NULL,
    CONSTRAINT batch_job_instance_un UNIQUE (job_name, job_key)
);

CREATE TABLE IF NOT EXISTS batch_job_execution (
    job_execution_id BIGINT NOT NULL PRIMARY KEY,
    version BIGINT,
    job_instance_id BIGINT NOT NULL,
    create_time TIMESTAMP(6) NOT NULL,
    start_time TIMESTAMP(6),
    end_time TIMESTAMP(6),
    status VARCHAR(10),
    exit_code VARCHAR(2500),
    exit_message VARCHAR(2500),
    last_updated TIMESTAMP(6),
    CONSTRAINT batch_job_execution_fk FOREIGN KEY (job_instance_id)
        REFERENCES batch_job_instance(job_instance_id)
);

CREATE TABLE IF NOT EXISTS batch_job_execution_params (
    job_execution_id BIGINT NOT NULL,
    parameter_name VARCHAR(100) NOT NULL,
    parameter_type VARCHAR(100) NOT NULL,
    parameter_value VARCHAR(2500),
    identifying CHAR(1) NOT NULL,
    CONSTRAINT batch_job_exec_params_fk FOREIGN KEY (job_execution_id)
        REFERENCES batch_job_execution(job_execution_id)
);

CREATE TABLE IF NOT EXISTS batch_step_execution (
    step_execution_id BIGINT NOT NULL PRIMARY KEY,
    version BIGINT NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    job_execution_id BIGINT NOT NULL,
    create_time TIMESTAMP(6) NOT NULL,
    start_time TIMESTAMP(6),
    end_time TIMESTAMP(6),
    status VARCHAR(10),
    commit_count BIGINT,
    read_count BIGINT,
    filter_count BIGINT,
    write_count BIGINT,
    read_skip_count BIGINT,
    write_skip_count BIGINT,
    process_skip_count BIGINT,
    rollback_count BIGINT,
    exit_code VARCHAR(2500),
    exit_message VARCHAR(2500),
    last_updated TIMESTAMP(6),
    CONSTRAINT batch_step_execution_fk FOREIGN KEY (job_execution_id)
        REFERENCES batch_job_execution(job_execution_id)
);

CREATE TABLE IF NOT EXISTS batch_step_execution_context (
    step_execution_id BIGINT NOT NULL PRIMARY KEY,
    short_context VARCHAR(2500) NOT NULL,
    serialized_context TEXT,
    CONSTRAINT batch_step_exec_ctx_fk FOREIGN KEY (step_execution_id)
        REFERENCES batch_step_execution(step_execution_id)
);

CREATE TABLE IF NOT EXISTS batch_job_execution_context (
    job_execution_id BIGINT NOT NULL PRIMARY KEY,
    short_context VARCHAR(2500) NOT NULL,
    serialized_context TEXT,
    CONSTRAINT batch_job_exec_ctx_fk FOREIGN KEY (job_execution_id)
        REFERENCES batch_job_execution(job_execution_id)
);

-- ------------------------------------------------------------------
-- Sequences Spring Batch expects on PostgreSQL
-- ------------------------------------------------------------------

CREATE SEQUENCE IF NOT EXISTS batch_job_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS batch_job_execution_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS batch_step_execution_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- ------------------------------------------------------------------
-- (Optional) quick sanity checks - uncomment to run manually
-- ------------------------------------------------------------------
-- SELECT nextval('batch_job_seq');
-- SELECT nextval('batch_job_execution_seq');
-- SELECT nextval('batch_step_execution_seq');

-- End of migration

package de.tsearch.clipcollector.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CheckClipsTask {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JobLauncher jobLauncher;

    private final Job job;

    public CheckClipsTask(JobLauncher jobLauncher, @Qualifier("updateAllClipsJob") Job job) {
        this.jobLauncher = jobLauncher;
        this.job = job;
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000, initialDelay = 3 * 60 * 60 * 1000)
    protected void checkAllClips() {
        logger.info("Start check all existing clips job");
        try {
            jobLauncher.run(job, new JobParametersBuilder().toJobParameters());
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            e.printStackTrace();
        }
    }
}

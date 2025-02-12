package common.utils;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A more robust generic scheduler service that handles Quartz job scheduling,
 * unscheduling, checking job status, and shutting down the scheduler.
 */
public class SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);
    private static volatile Scheduler scheduler;

    private SchedulerService() {
        // Prevent instantiation
    }

    /**
     * Lazily initializes and returns a single Scheduler instance.
     * 
     * @return the Scheduler singleton
     * @throws SchedulerException if there's a problem creating or retrieving the Scheduler
     */
    public static Scheduler getScheduler() throws SchedulerException {
        if (scheduler == null) {
            synchronized (SchedulerService.class) {
                if (scheduler == null) {
                    scheduler = StdSchedulerFactory.getDefaultScheduler();
                    scheduler.start();
                    logger.info("Quartz Scheduler started successfully.");
                }
            }
        }
        return scheduler;
    }

    /**
     * Schedules a job with a given trigger. Allows advanced trigger configurations
     * (SimpleTrigger, CronTrigger, etc.).
     *
     * @param jobClass   the class implementing org.quartz.Job
     * @param jobName    the name of the job
     * @param groupName  the job group name
     * @param trigger    the trigger to associate with the job
     * @param jobDataMap optional data map for the job
     */
    public static void scheduleJob(Class<? extends Job> jobClass,
                                   String jobName,
                                   String groupName,
                                   Trigger trigger,
                                   JobDataMap jobDataMap) {
        try {
            Scheduler sched = getScheduler();
            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(jobName, groupName)
                    .usingJobData(jobDataMap == null ? new JobDataMap() : jobDataMap)
                    .build();

            sched.scheduleJob(jobDetail, trigger);
            logger.info("Job [{}] in group [{}] scheduled with trigger [{}].",
                        jobName, groupName, trigger.getKey().getName());
        } catch (SchedulerException e) {
            logger.error("Error scheduling job [{}] in group [{}].", jobName, groupName, e);
        }
    }

    /**
     * Convenience method to create and schedule a job with a simple interval trigger.
     *
     * @param jobClass      the class implementing org.quartz.Job
     * @param jobName       the job name
     * @param groupName     the job group name
     * @param intervalInSec the repeat interval in seconds
     * @param repeatCount   the number of repeats (SimpleTrigger.REPEAT_INDEFINITELY for infinite)
     * @param jobDataMap    optional job data map
     */
    public static void scheduleSimpleJob(Class<? extends Job> jobClass,
                                         String jobName,
                                         String groupName,
                                         int intervalInSec,
                                         int repeatCount,
                                         JobDataMap jobDataMap) {
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName + "_trigger", groupName)
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(intervalInSec)
                        .withRepeatCount(repeatCount))
                .build();

        scheduleJob(jobClass, jobName, groupName, trigger, jobDataMap);
    }

    /**
     * Convenience method to create and schedule a job using a Cron expression.
     *
     * @param jobClass    the class implementing org.quartz.Job
     * @param jobName     the job name
     * @param groupName   the job group name
     * @param cronExpr    the Cron expression
     * @param jobDataMap  optional job data map
     */
    public static void scheduleCronJob(Class<? extends Job> jobClass,
                                       String jobName,
                                       String groupName,
                                       String cronExpr,
                                       JobDataMap jobDataMap) {
        try {
            CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(cronExpr);
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + "_cronTrigger", groupName)
                    .withSchedule(cronSchedule)
                    .build();

            scheduleJob(jobClass, jobName, groupName, trigger, jobDataMap);
        } catch (RuntimeException e) {
            logger.error("Invalid cron expression [{}]. Job [{}] in group [{}] not scheduled.", 
                          cronExpr, jobName, groupName, e);
        }
    }

    /**
     * Checks if a job is already scheduled.
     *
     * @param jobName   the job name
     * @param groupName the job group name
     * @return true if job with given key exists, false otherwise
     */
    public static boolean isJobScheduled(String jobName, String groupName) {
        try {
            Scheduler sched = getScheduler();
            JobKey jobKey = new JobKey(jobName, groupName);
            return sched.checkExists(jobKey);
        } catch (SchedulerException e) {
            logger.error("Error checking existence of job [{}] in group [{}].", jobName, groupName, e);
            return false;
        }
    }

    /**
     * Unschedules (deletes) a job by its name and group.
     *
     * @param jobName   the job name
     * @param groupName the job group name
     * @return true if the job was found and deleted, false otherwise
     */
    public static boolean unscheduleJob(String jobName, String groupName) {
        try {
            Scheduler sched = getScheduler();
            JobKey jobKey = new JobKey(jobName, groupName);
            if (sched.checkExists(jobKey)) {
                return sched.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            logger.error("Error unscheduling job [{}] in group [{}].", jobName, groupName, e);
        }
        return false;
    }

    /**
     * Updates the trigger of an existing job with a new trigger (e.g., new schedule).
     *
     * @param jobName   the name of the existing job
     * @param groupName the job group name
     * @param newTrigger the new trigger to apply
     */
    public static void updateJobTrigger(String jobName, String groupName, Trigger newTrigger) {
        try {
            Scheduler sched = getScheduler();
            JobKey jobKey = new JobKey(jobName, groupName);

            if (sched.checkExists(jobKey)) {
                // Retrieve existing job detail
                JobDetail jobDetail = sched.getJobDetail(jobKey);

                // Unschedule the existing trigger(s)
                for (TriggerKey tKey : sched.getTriggersOfJob(jobKey).stream()
                                            .map(Trigger::getKey).toList()) {
                    sched.unscheduleJob(tKey);
                }

                // Schedule new trigger
                sched.scheduleJob(jobDetail, newTrigger);
                logger.info("Updated trigger for job [{}] in group [{}].", jobName, groupName);
            } else {
                logger.warn("Job [{}] in group [{}] does not exist. Cannot update trigger.", jobName, groupName);
            }
        } catch (SchedulerException e) {
            logger.error("Error updating trigger for job [{}] in group [{}].", jobName, groupName, e);
        }
    }

    /**
     * Shuts down the scheduler gracefully.
     *
     * @param waitForJobsToComplete if true, the scheduler will wait for all running jobs to finish before shutting down
     */
    public static void shutdownScheduler(boolean waitForJobsToComplete) {
        try {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown(waitForJobsToComplete);
                logger.info("Quartz Scheduler shut down. waitForJobsToComplete={}", waitForJobsToComplete);
            }
        } catch (SchedulerException e) {
            logger.error("Error shutting down scheduler.", e);
        }
    }
}

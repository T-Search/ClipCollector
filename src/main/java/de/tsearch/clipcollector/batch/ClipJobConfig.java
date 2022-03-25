package de.tsearch.clipcollector.batch;

import de.tsearch.clipcollector.database.postgres.converter.TClipConverter;
import de.tsearch.clipcollector.database.postgres.entity.Clip;
import de.tsearch.clipcollector.database.postgres.repository.ClipRepository;
import de.tsearch.tclient.ClipClient;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Configuration
public class ClipJobConfig {

    private final EntityManagerFactory entityManagerFactory;

    private final ClipRepository clipRepository;

    private final ClipClient clipClient;

    private final TClipConverter tClipConverter;

    public ClipJobConfig(EntityManagerFactory entityManagerFactory, ClipRepository clipRepository, ClipClient clipClient, TClipConverter tClipConverter) {
        this.entityManagerFactory = entityManagerFactory;
        this.clipRepository = clipRepository;
        this.clipClient = clipClient;
        this.tClipConverter = tClipConverter;
    }

    @Bean(name = "updateAllClipsJob")
    public Job updateAllClipsJob(JobBuilderFactory jobBuilders,
                                 StepBuilderFactory stepBuilders) {
        return jobBuilders.get("updateAllClipsJob")
                .start(updateAllClipsStep1(stepBuilders))
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step updateAllClipsStep1(StepBuilderFactory stepBuilders) {
        return stepBuilders.get("getAndWriteTwitchInformation").<Clip, Clip>chunk(10)
                .reader(clipsItemReader())
                .processor(updateAllClipsProcessor1())
                .writer(clipsItemWriter())
                .allowStartIfComplete(true)
                .build();
    }

    public JpaPagingItemReader<Clip> clipsItemReader() {
        return new JpaPagingItemReaderBuilder<Clip>()
                .name("clipsReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT c FROM Clip c ORDER BY id")
                .pageSize(100)
                .build();
    }

    public ClipItemProcessor updateAllClipsProcessor1() {
        return new ClipItemProcessor(clipRepository, clipClient, tClipConverter);
    }

    public JpaItemWriter<Clip> clipsItemWriter() {
        return new JpaItemWriterBuilder<Clip>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}

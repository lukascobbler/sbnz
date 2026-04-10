package com.luka.kbpdm.configuration;

import com.luka.kbpdm.simulation.machines.MachineProcessProfile;
import com.luka.kbpdm.simulation.machines.MachineProcessRegistry;
import com.luka.kbpdm.simulation.machines.MetricProfile;
import org.drools.template.DataProviderCompiler;
import org.drools.template.objects.ArrayDataProvider;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DroolsConfig {

    private static final String RULES_PATH = "rules/rules.drl";
    private static final String THRESHOLD_TEMPLATE_PATH = "rules/templates_thresholds.drt";
    private static final String TRENDS_TEMPLATE_PATH = "rules/templates_trends.drt";
    private static final String GENERATED_THRESHOLDS_KFS_PATH = "src/main/resources/rules/generated_thresholds.drl";
    private static final String GENERATED_THRESHOLDS_DEV_PATH = "src/main/resources/rules/generated_thresholds.drl";
    private static final String GENERATED_TRENDS_KFS_PATH = "src/main/resources/rules/generated_trends.drl";
    private static final String GENERATED_TRENDS_DEV_PATH = "src/main/resources/rules/generated_trends.drl";

    @Bean
    public KieContainer kieContainer(MachineProcessRegistry machineRegistry) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        KieModuleModel module = ks.newKieModuleModel();
        KieBaseModel base = module.newKieBaseModel("rules-base")
                .setDefault(true)
                .addPackage("rules")
                .setEventProcessingMode(EventProcessingOption.STREAM);
        base.newKieSessionModel("rules-session")
                .setDefault(true)
                .setType(KieSessionModel.KieSessionType.STATEFUL)
                .setClockType(ClockTypeOption.get("pseudo"));

        kfs.writeKModuleXML(module.toXML());
        kfs.write(ks.getResources().newClassPathResource(RULES_PATH));

        writeGeneratedThresholdRules(ks, kfs, machineRegistry);
        writeGeneratedTrendRules(ks, kfs, machineRegistry);

        KieBuilder builder = ks.newKieBuilder(kfs);
        builder.buildAll();
        if (builder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new IllegalStateException("Drools build failed:\n" + builder.getResults());
        }
        KieRepository repo = ks.getRepository();
        return ks.newKieContainer(repo.getDefaultReleaseId());
    }

    private void writeGeneratedThresholdRules(KieServices ks, KieFileSystem kfs, MachineProcessRegistry registry) {
        List<String[]> rows = new ArrayList<>();
        for (MachineProcessProfile machine : registry.profilesInOrder()) {
            for (MetricProfile metric : machine.metrics()) {
                if (!metric.hasAnomalyThreshold()) {
                    continue;
                }
                String ruleName = "Threshold: " + machine.machineId() + " " + metric.metricKey() + " high";
                String desc = metric.displayName() + " reached or exceeded the configured high band for this machine.";
                rows.add(new String[]{
                        ruleName,
                        machine.machineId(),
                        metric.metricKey(),
                        Double.toString(metric.anomalyThreshold()),
                        desc
                });
            }
        }
        String drl = compileTemplate(THRESHOLD_TEMPLATE_PATH, rows);
        writeGeneratedDrl(ks, kfs, drl, GENERATED_THRESHOLDS_KFS_PATH, GENERATED_THRESHOLDS_DEV_PATH);
    }

    private void writeGeneratedTrendRules(KieServices ks, KieFileSystem kfs, MachineProcessRegistry registry) {
        List<String[]> rows = new ArrayList<>();
        for (MachineProcessProfile machine : registry.profilesInOrder()) {
            for (MetricProfile metric : machine.metrics()) {
                if (!metric.trendEnabled()) {
                    continue;
                }
                String metricName = metric.displayName() + " (" + metric.metricKey() + ")";
                rows.add(new String[]{
                        "CEP: " + machine.machineId() + " " + metric.metricKey() + " rising trend",
                        "CEP: " + machine.machineId() + " " + metric.metricKey() + " rising trend cleared",
                        machine.machineId(),
                        metric.metricKey(),
                        metricName + " rose 10 consecutive ticks (strictly increasing each sub-step)."
                });
            }
        }
        String drl = compileTemplate(TRENDS_TEMPLATE_PATH, rows);
        writeGeneratedDrl(ks, kfs, drl, GENERATED_TRENDS_KFS_PATH, GENERATED_TRENDS_DEV_PATH);
    }

    private String compileTemplate(String templatePath, List<String[]> rows) {
        DataProviderCompiler compiler = new DataProviderCompiler();
        InputStream templateStream = getClass().getResourceAsStream("/" + templatePath);
        if (templateStream == null) {
            throw new IllegalStateException("Missing rule template on classpath: " + templatePath);
        }
        return compiler.compile(new ArrayDataProvider(rows.toArray(new String[0][])), templateStream);
    }

    private void writeGeneratedDrl(
            KieServices ks,
            KieFileSystem kfs,
            String drl,
            String kfsPath,
            String devPath
    ) {
        kfs.write(
                ks.getResources()
                        .newByteArrayResource(drl.getBytes(StandardCharsets.UTF_8))
                        .setResourceType(ResourceType.DRL)
                        .setSourcePath(kfsPath)
        );
        try {
            Path out = Path.of(devPath);
            Files.createDirectories(out.getParent());
            Files.writeString(out, drl, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
    }
}

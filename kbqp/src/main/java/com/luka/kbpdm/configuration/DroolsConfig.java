package com.luka.kbpdm.configuration;

import com.luka.kbpdm.domain.AnomalyType;
import com.luka.kbpdm.domain.TelemetryMetric;
import com.luka.kbpdm.simulation.machines.MachineProcessProfile;
import com.luka.kbpdm.simulation.machines.MachineProcessRegistry;
import org.drools.template.DataProviderCompiler;
import org.drools.template.objects.ArrayDataProvider;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.Message;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
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
    private static final String TEMPLATE_PATH = "rules/templates_thresholds.drt";
    private static final String TRENDS_TEMPLATE_PATH = "rules/templates_trends.drt";
    private static final String GENERATED_RULES_KFS_PATH = "src/main/resources/rules/generated_thresholds.drl";
    private static final String GENERATED_RULES_DEV_PATH = "src/main/resources/rules/generated_thresholds.drl";
    private static final String GENERATED_TRENDS_KFS_PATH = "src/main/resources/rules/generated_trends.drl";
    private static final String GENERATED_TRENDS_DEV_PATH = "src/main/resources/rules/generated_trends.drl";

    @Bean
    public KieContainer kieContainer(MachineProcessRegistry machineProcessRegistry) {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        KieModuleModel moduleModel = kieServices.newKieModuleModel();
        KieBaseModel baseModel = moduleModel
                .newKieBaseModel("rules-base")
                .setDefault(true)
                .addPackage("rules")
                .setEventProcessingMode(EventProcessingOption.STREAM);
        baseModel
                .newKieSessionModel("rules-session")
                .setDefault(true)
                .setType(KieSessionModel.KieSessionType.STATEFUL)
                .setClockType(ClockTypeOption.get("pseudo"));

        kieFileSystem.writeKModuleXML(moduleModel.toXML());
        kieFileSystem.write(kieServices.getResources().newClassPathResource(RULES_PATH));

        List<String[]> templateRows = new ArrayList<>();
        for (MachineProcessProfile p : machineProcessRegistry.profilesInOrder()) {
            templateRows.add(new String[]{
                    "Threshold: " + p.machineId() + " high temperature",
                    p.machineId(),
                    TelemetryMetric.TEMPERATURE_C.name(),
                    Double.toString(p.tempAnomalyThresholdC()),
                    AnomalyType.TEMPERATURE_ABOVE_THRESHOLD.name(),
                    "Temperature reached or exceeded the configured high band for this machine."
            });
            templateRows.add(new String[]{
                    "Threshold: " + p.machineId() + " high vibration",
                    p.machineId(),
                    TelemetryMetric.VIBRATION_RMS.name(),
                    Double.toString(p.vibAnomalyThresholdRms()),
                    AnomalyType.VIBRATION_ABOVE_THRESHOLD.name(),
                    "Vibration reached or exceeded the configured high band for this machine."
            });
        }

        DataProviderCompiler compiler = new DataProviderCompiler();
        InputStream templateStream = getClass().getResourceAsStream("/" + TEMPLATE_PATH);
        if (templateStream == null) {
            throw new IllegalStateException("Missing rule template on classpath: " + TEMPLATE_PATH);
        }
        String[][] rows = templateRows.toArray(new String[0][]);
        String compiledDrl = compiler.compile(new ArrayDataProvider(rows), templateStream);

        kieFileSystem.write(
                kieServices.getResources()
                        .newByteArrayResource(compiledDrl.getBytes(StandardCharsets.UTF_8))
                        .setResourceType(ResourceType.DRL)
                        .setSourcePath(GENERATED_RULES_KFS_PATH)
        );

        try {
            Path out = Path.of(GENERATED_RULES_DEV_PATH);
            Files.createDirectories(out.getParent());
            Files.writeString(out, compiledDrl, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }

        String[][] trendRows = new String[][]{
                {
                        "CEP: Temperature rising trend",
                        "CEP: Temperature rising trend cleared",
                        "TEMPERATURE_RISING_TREND",
                        "temperatureC",
                        "Temperature rose 10 consecutive ticks (strictly increasing each sub-step).",
                },
                {
                        "CEP: Vibration rising trend",
                        "CEP: Vibration rising trend cleared",
                        "VIBRATION_RISING_TREND",
                        "vibrationRms",
                        "Vibration RMS rose 10 consecutive ticks (strictly increasing each sub-step).",
                },
        };
        InputStream trendsTemplateStream = getClass().getResourceAsStream("/" + TRENDS_TEMPLATE_PATH);
        if (trendsTemplateStream == null) {
            throw new IllegalStateException("Missing rule template on classpath: " + TRENDS_TEMPLATE_PATH);
        }
        String compiledTrends = compiler.compile(new ArrayDataProvider(trendRows), trendsTemplateStream);
        kieFileSystem.write(
                kieServices.getResources()
                        .newByteArrayResource(compiledTrends.getBytes(StandardCharsets.UTF_8))
                        .setResourceType(ResourceType.DRL)
                        .setSourcePath(GENERATED_TRENDS_KFS_PATH)
        );
        try {
            Path out = Path.of(GENERATED_TRENDS_DEV_PATH);
            Files.createDirectories(out.getParent());
            Files.writeString(out, compiledTrends, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new IllegalStateException("Drools build failed:\n" + kieBuilder.getResults());
        }
        KieRepository kieRepository = kieServices.getRepository();

        return kieServices.newKieContainer(kieRepository.getDefaultReleaseId());
    }
}

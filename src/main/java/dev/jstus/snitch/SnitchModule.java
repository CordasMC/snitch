package dev.jstus.snitch;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import dev.jstus.snitch.command.CommandModule;
import dev.jstus.snitch.config.ConfigModule;
import dev.jstus.snitch.db.DatabaseModule;
import dev.jstus.snitch.message.MessageModule;
import dev.jstus.snitch.report.ReportModule;
import dev.jstus.snitch.shadowban.ShadowBanModule;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;

@AllArgsConstructor
public class SnitchModule extends AbstractModule {

    private final Snitch plugin;

    @Provides
    @Singleton
    Logger logger() {
        return plugin.getSLF4JLogger();
    }

    @Override
    public void configure() {
        bind(Snitch.class).toInstance(plugin);

        install(new ConfigModule(Snitch.class, plugin.getDataFolder().toPath()));
        install(new DatabaseModule(plugin.getDataFolder().toPath()));
        install(new ReportModule());
        install(new ShadowBanModule());
        install(new CommandModule());
        install(new MessageModule());
    }
}

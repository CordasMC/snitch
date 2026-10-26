package dev.jstus.snitch;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Stage;
import com.zaxxer.hikari.HikariDataSource;
import dev.jstus.snitch.db.DatabaseExecutor;
import dev.jstus.snitch.profile.ProfileCache;
import java.util.concurrent.ExecutorService;
import javax.sql.DataSource;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.invui.InvUI;

@Getter
public class Snitch extends JavaPlugin {

    @Getter
    public static Snitch instance;

    private Injector injector;

    @Override
    public void onEnable() {
        instance = this;
        InvUI.getInstance().setPlugin(instance);

        injector = Guice.createInjector(Stage.PRODUCTION, new SnitchModule(this));
    }

    @Override
    public void onDisable() {
        if (injector == null) return;

        injector.getInstance(ProfileCache.class).shutdown();
        injector.getInstance(Key.get(ExecutorService.class, DatabaseExecutor.class)).close();
        if (injector.getInstance(DataSource.class) instanceof HikariDataSource hikari) {
            hikari.close();
        }
    }
}

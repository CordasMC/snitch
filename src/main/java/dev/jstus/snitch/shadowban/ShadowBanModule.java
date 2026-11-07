package dev.jstus.snitch.shadowban;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public final class ShadowBanModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ShadowBanService.class).to(SimpleShadowBanService.class);

        bind(ShadowBanRepository.class).to(JooqShadowBanRepository.class).in(Singleton.class);
    }
}

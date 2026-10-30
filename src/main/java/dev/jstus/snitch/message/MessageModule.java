package dev.jstus.snitch.message;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import dev.jstus.snitch.config.impl.MessageConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

public class MessageModule extends AbstractModule {

    @Override
    public void configure() {

    }

    @Provides
    @Singleton
    public MiniMessage provideMiniMessage(MessageConfig msgConf) {
        return MiniMessage.builder()
                .tags(TagResolver.resolver(StandardTags.defaults(), Placeholder.parsed("prefix", msgConf.getGlobalPrefix())))
                .build();
    }
}

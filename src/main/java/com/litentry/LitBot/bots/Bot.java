package com.litentry.LitBot.bots;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.litentry.LitBot.config.BotProperties;
import com.litentry.LitBot.config.Constants;
import com.litentry.LitBot.handlers.CommandHandler;
import com.litentry.LitBot.handlers.PrivateMsgHandler;
import com.litentry.LitBot.handlers.ReactionHandler;
import com.litentry.LitBot.listeners.Listener;
import com.litentry.LitBot.service.PolkadotVerifyService;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.stereotype.Component;
import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class Bot {
    private final ScheduledExecutorService threadpool;
    private final EventWaiter waiter;
    private final CommandHandler cmdHandler;
    private final ReactionHandler reactionHandler;
    private PrivateMsgHandler privateMsgHandler;
    private BotProperties botProperties;
    private PolkadotVerifyService polkadotVerifyService;

    public Bot(BotProperties botProperties, PolkadotVerifyService polkadotVerifyService) throws LoginException {
        this.botProperties = botProperties;
        this.polkadotVerifyService = polkadotVerifyService;

        int threadpoolSize = 10;
        threadpool = Executors.newScheduledThreadPool(threadpoolSize);
        waiter = new EventWaiter();
        reactionHandler = new ReactionHandler(this);
        privateMsgHandler = new PrivateMsgHandler(this.botProperties, this.polkadotVerifyService);
        privateMsgHandler.setWaiter(waiter);
        cmdHandler = new CommandHandler(this, this.botProperties, this.polkadotVerifyService);

        EmbedUtils.setEmbedBuilder(() -> new EmbedBuilder().setColor(Constants.BOT_EMBED).setFooter(botProperties.getFooter()));

        JDA jda = JDABuilder
                .createDefault(botProperties.getToken())
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGES
                )
                .setMemberCachePolicy(MemberCachePolicy.ONLINE)
                .enableCache(CacheFlag.CLIENT_STATUS)
                .setChunkingFilter(ChunkingFilter.NONE)
                .setMaxBufferSize(40960)
                .addEventListeners(new Listener(this, this.botProperties), waiter, cmdHandler, reactionHandler, privateMsgHandler)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing("Booting..."))
                .build();

        //wait for loading all resources
        //jda.awaitReady();

        //log.info("started LITBot: {}", this);
        System.out.println("started LITBot");
        polkadotVerifyService.setJDA(jda);
    }

    public ScheduledExecutorService getThreadpool() {
        return threadpool;
    }

    public EventWaiter getWaiter() {
        return waiter;
    }

    public CommandHandler getCmdHandler() {
        return cmdHandler;
    }

    public ReactionHandler getReactionHandler() {
        return reactionHandler;
    }
}

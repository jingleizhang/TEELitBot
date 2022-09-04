package com.litentry.LitBot.listeners;

import com.litentry.LitBot.bots.Bot;
import com.litentry.LitBot.common.PresenceType;
import com.litentry.LitBot.config.BotProperties;
import com.litentry.LitBot.utils.BotUtils;
import com.litentry.LitBot.utils.WebhookUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

@Service
public class Listener implements EventListener {

    //private static final Logger log = LoggerFactory.getLogger(Listener.class);

    private Bot bot;
    private BotProperties botProperties;
    private final WebhookUtil webhookUtil;

    public Listener(Bot bot, BotProperties botProperties) {
        this.bot = bot;
        this.botProperties = botProperties;

        this.webhookUtil = new WebhookUtil(botProperties.getWebHook());
    }

    public void onReady(@Nonnull ReadyEvent event) {
        final JDA jda = event.getJDA();
        System.out.println("ready");

        for (Guild guild : jda.getGuilds()) {
            updateSlashCmds(guild);
        }

        // Update Presence
        bot.getThreadpool().scheduleWithFixedDelay(() -> BotUtils.updatePresence(jda, PresenceType.MEMBERS), 0, 30, TimeUnit.MINUTES);
        bot.getThreadpool().scheduleWithFixedDelay(() -> bot.getCmdHandler().cleanCooldowns(), 1, 1, TimeUnit.DAYS);
    }

    private void onGuildJoin(@Nonnull GuildJoinEvent event) {
        final Guild guild = event.getGuild();
        guild
            .retrieveOwner()
            .queue(owner -> {
                //log.info("Guild Joined - GuildID: {} | OwnerId: {} | Members: {}", guild.getId(), owner.getId(), guild.getMemberCount());
                webhookUtil.sendWebhook(owner, guild, WebhookUtil.Action.JOIN);

                updateSlashCmds(guild);
            });
    }

    private void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        final Guild guild = event.getGuild();
        guild
            .retrieveOwner()
            .queue(owner -> {
                //log.info("Guild Left - GuildID: {} | OwnerId: {} | Members: {}", guild.getId(), owner.getId(), guild.getMemberCount());
                webhookUtil.sendWebhook(owner, guild, WebhookUtil.Action.LEAVE);
            });
    }

    private void updateSlashCmds(Guild guild) {
        guild
            .updateCommands()
            .addCommands(Commands.slash("start", "start captcha challenge, to verify that you are not a bot."))
            .addCommands(Commands.slash("connect", "connect ID-Hub account and finish the verification."))
            .addCommands(Commands.slash("verify", "automatically query your predetermined roles from ID-Hub."))
            //.addCommands(Commands.slash("modmail", "mod mail tests"))
            .queue();
    }

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            this.onReady((ReadyEvent) event);
        } else if (event instanceof GuildJoinEvent) {
            this.onGuildJoin((GuildJoinEvent) event);
        } else if (event instanceof GuildLeaveEvent) {
            this.onGuildLeave((GuildLeaveEvent) event);
        }
    }
}

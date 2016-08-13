/*
 * This file is part of LanternServer, licensed under the MIT License (MIT).
 *
 * Copyright (c) LanternPowered <https://github.com/LanternPowered>
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.lanternpowered.server.entity.living.player;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.lanternpowered.server.text.translation.TranslationHelper.t;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Sets;
import org.lanternpowered.server.bossbar.LanternBossBar;
import org.lanternpowered.server.data.io.store.entity.PlayerStore;
import org.lanternpowered.server.data.key.LanternKeys;
import org.lanternpowered.server.effect.AbstractViewer;
import org.lanternpowered.server.effect.sound.LanternSoundType;
import org.lanternpowered.server.entity.LanternEntityHumanoid;
import org.lanternpowered.server.entity.living.player.gamemode.LanternGameMode;
import org.lanternpowered.server.entity.living.player.tab.GlobalTabList;
import org.lanternpowered.server.entity.living.player.tab.GlobalTabListEntry;
import org.lanternpowered.server.entity.living.player.tab.LanternTabList;
import org.lanternpowered.server.entity.living.player.tab.LanternTabListEntry;
import org.lanternpowered.server.entity.living.player.tab.LanternTabListEntryBuilder;
import org.lanternpowered.server.game.Lantern;
import org.lanternpowered.server.game.LanternGame;
import org.lanternpowered.server.game.registry.type.block.BlockRegistryModule;
import org.lanternpowered.server.inventory.HumanInventoryContainer;
import org.lanternpowered.server.inventory.LanternContainer;
import org.lanternpowered.server.inventory.PlayerContainerSession;
import org.lanternpowered.server.inventory.entity.LanternHumanInventory;
import org.lanternpowered.server.network.NetworkSession;
import org.lanternpowered.server.network.objects.LocalizedText;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayInOutBrand;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayOutBlockChange;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayOutChatMessage;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayOutParticleEffect;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayOutPlayerJoinGame;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayOutPlayerPositionAndLook;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayOutPlayerRespawn;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayOutSendResourcePack;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayOutSetReducedDebug;
import org.lanternpowered.server.permission.AbstractSubject;
import org.lanternpowered.server.profile.LanternGameProfile;
import org.lanternpowered.server.scoreboard.LanternScoreboard;
import org.lanternpowered.server.text.title.LanternTitles;
import org.lanternpowered.server.world.LanternWorld;
import org.lanternpowered.server.world.LanternWorldProperties;
import org.lanternpowered.server.world.chunk.ChunkLoadingTicket;
import org.lanternpowered.server.world.difficulty.LanternDifficulty;
import org.lanternpowered.server.world.dimension.LanternDimensionType;
import org.lanternpowered.server.world.rules.RuleTypes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.data.type.SkinParts;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.chat.ChatVisibilities;
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.util.RelativePositions;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.ChunkTicketManager;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@NonnullByDefault
public class LanternPlayer extends LanternEntityHumanoid implements AbstractSubject, Player, AbstractViewer {

    private final LanternUser user;
    private final LanternGameProfile gameProfile;
    private final NetworkSession session;

    private final LanternTabList tabList = new LanternTabList(this);

    private MessageChannel messageChannel = MessageChannel.TO_ALL;

    // The (client) locale of the player
    private Locale locale = Locale.ENGLISH;

    // The (client) render distance of the player
    // When specified -1, the render distance will match the server one
    private int viewDistance = -1;

    // The chat visibility
    private ChatVisibility chatVisibility = ChatVisibilities.FULL;

    // The main hand of the player
    private HandType mainHand = HandType.RIGHT;

    // Whether the chat colors are enabled
    private boolean chatColorsEnabled;

    // The visible skin parts
    private Set<SkinPart> skinParts = Sets.newHashSet(SkinParts.CAPE, SkinParts.HAT, SkinParts.JACKET, SkinParts.LEFT_SLEEVE,
            SkinParts.LEFT_PANTS_LEG, SkinParts.RIGHT_SLEEVE, SkinParts.RIGHT_PANTS_LEG);

    private LanternScoreboard scoreboard;

    // Whether you should ignore this player when checking for sleeping players to reset the time
    private boolean sleepingIgnored;

    // The chunks the client knowns about
    private final Set<Vector2i> knownChunks = new HashSet<>();

    // The interaction handler
    private final PlayerInteractionHandler interactionHandler;

    // The chunk position since the last #pulseChunkChanges call
    private Vector2i lastChunkPos = null;

    // The loading ticket that will force the chunks to be loaded
    @Nullable private ChunkTicketManager.PlayerEntityLoadingTicket loadingTicket;

    // All the resource packs that are send to the client
    // and are waiting for a response
    private final List<ResourcePack> pendingResourcePacksForStatus = new ArrayList<>();

    /**
     * The inventory of the {@link Player}.
     */
    private final LanternHumanInventory inventory;

    /**
     * The {@link LanternContainer} of the players inventory.
     */
    private final HumanInventoryContainer inventoryContainer;

    /**
     * The container session of this {@link Player}.
     */
    private final PlayerContainerSession containerSession;

    /**
     * All the boss bars that are visible for this {@link Player}.
     */
    private final Set<LanternBossBar> bossBars = new HashSet<>();

    /**
     * The last time that the player was active.
     */
    private long lastActiveTime;

    /**
     * This field is for internal use only, it is used while finding a proper
     * world to spawn the player in. Used at {@link NetworkSession#initPlayer()} and
     * {@link PlayerStore}.
     */
    @Nullable private LanternWorldProperties tempWorld;

    public LanternPlayer(LanternGameProfile gameProfile, NetworkSession session) {
        super(checkNotNull(gameProfile, "gameProfile").getUniqueId());
        this.interactionHandler = new PlayerInteractionHandler(this);
        this.inventory = new LanternHumanInventory(null, null, this);
        this.inventoryContainer = new HumanInventoryContainer(null, this.inventory);
        this.containerSession = new PlayerContainerSession(this);
        this.session = session;
        this.gameProfile = gameProfile;
        // Get or create the user object
        this.user = (LanternUser) Sponge.getServiceManager().provideUnchecked(UserStorageService.class)
                .getOrCreate(gameProfile);
        this.user.setPlayer(this);
        this.resetIdleTimeoutCounter();
    }

    public Set<LanternBossBar> getBossBars() {
        return this.bossBars;
    }

    /**
     * Resets the timeout counter.
     */
    public void resetIdleTimeoutCounter() {
        this.lastActiveTime = System.currentTimeMillis();
    }

    @Override
    public void registerKeys() {
        super.registerKeys();
        this.registerKey(Keys.LAST_DATE_PLAYED, null);
        this.registerKey(Keys.FIRST_DATE_PLAYED, null);
        this.registerKey(Keys.IS_FLYING, false).nonRemovableAttachedValueProcessor();
        this.registerKey(Keys.FLYING_SPEED, 0.1).nonRemovableAttachedValueProcessor();
        this.registerKey(Keys.CAN_FLY, false).nonRemovableAttachedValueProcessor();
        this.registerKey(Keys.RESPAWN_LOCATIONS, new HashMap<>()).nonRemovableAttachedValueProcessor();
        this.registerKey(Keys.GAME_MODE, GameModes.NOT_SET).nonRemovableAttachedValueProcessor();
        this.registerKey(LanternKeys.SCORE, 0).nonRemovableAttachedValueProcessor();
    }

    @Nullable
    public LanternWorldProperties getTempWorld() {
        return this.tempWorld;
    }

    public void setTempWorld(@Nullable LanternWorldProperties tempTargetWorld) {
        this.tempWorld = tempTargetWorld;
    }

    @Override
    public String getName() {
        return this.gameProfile.getName().get();
    }

    /**
     * Sets the {@link LanternWorld} without triggering
     * any changes for this player.
     *
     * @param world The world
     */
    public void setRawWorld(@Nullable LanternWorld world) {
        super.setWorld(world);
    }

    @Override
    public void setWorld(@Nullable LanternWorld world) {
        LanternWorld oldWorld = this.getWorld();
        if (oldWorld != world) {
            this.interactionHandler.reset();
        }
        super.setWorld(world);
        if (world == oldWorld) {
            return;
        }
        if (oldWorld != null) {
            if (this.loadingTicket != null) {
                this.loadingTicket.release();
                this.loadingTicket = null;
            }
            // Remove the player from all the observed chunks, there is no need
            // to send unload messages because we will respawn in a different world
            final ObservedChunkManager observedChunkManager = oldWorld.getObservedChunkManager();
            final Set<Vector2i> knownChunks = new HashSet<>(this.knownChunks);
            knownChunks.forEach(coords -> observedChunkManager.removeObserver(coords, this, false));
            this.knownChunks.clear();
            // Clear the last chunk pos
            this.lastChunkPos = null;
            // Remove the player from the world
            oldWorld.removePlayer(this);
        }
        if (world != null) {
            LanternGameMode gameMode = (LanternGameMode) this.get(Keys.GAME_MODE).get();
            LanternDimensionType dimensionType = (LanternDimensionType) world.getDimension().getType();
            LanternDifficulty difficulty = (LanternDifficulty) world.getDifficulty();
            boolean reducedDebug = world.getOrCreateRule(RuleTypes.REDUCED_DEBUG_INFO).getValue();
            // The player has joined the server
            if (oldWorld == null) {
                this.session.getServer().addPlayer(this);
                this.session.send(new MessagePlayOutPlayerJoinGame(gameMode, dimensionType, difficulty, this.getEntityId(),
                        this.session.getServer().getMaxPlayers(), reducedDebug, false));
                // Send the server brand
                this.session.send(new MessagePlayInOutBrand(LanternGame.IMPL_NAME));
                // Send the player list
                List<LanternTabListEntry> tabListEntries = new ArrayList<>();
                LanternTabListEntryBuilder thisBuilder = createTabListEntryBuilder(this);
                for (Player player : Sponge.getServer().getOnlinePlayers()) {
                    LanternTabListEntryBuilder builder = player == this ? thisBuilder : createTabListEntryBuilder((LanternPlayer) player);
                    tabListEntries.add(builder.list(this.tabList).build());
                    if (player != this) {
                        player.getTabList().addEntry(thisBuilder.list(player.getTabList()).build());
                    }
                }
                this.tabList.init(tabListEntries);
            } else {
                if (oldWorld != null && oldWorld != world) {
                    LanternDimensionType oldDimensionType = (LanternDimensionType) oldWorld.getDimension().getType();
                    // The client only creates a new world instance on the client if a
                    // different dimension is used, that is why we will send two respawn
                    // messages to trick the client to do it anyway
                    // This is also needed to avoid weird client bugs
                    if (oldDimensionType == dimensionType) {
                        oldDimensionType = (LanternDimensionType) (dimensionType == DimensionTypes.OVERWORLD ? DimensionTypes.NETHER :
                                DimensionTypes.OVERWORLD);
                        this.session.send(new MessagePlayOutPlayerRespawn(gameMode, oldDimensionType, difficulty));
                    }
                }
                // Send a respawn message
                this.session.send(new MessagePlayOutPlayerRespawn(gameMode, dimensionType, difficulty));
                this.session.send(new MessagePlayOutSetReducedDebug(reducedDebug));
            }
            // Add the player to the world
            world.addPlayer(this);
            // Send the first chunks
            this.pulseChunkChanges();
            final Vector3d position = this.getPosition();
            final Vector3d rotation = this.getRotation();
            this.session.send(world.getProperties().createWorldBorderMessage());
            world.getWeatherUniverse().ifPresent(u -> this.session.send(u.createSkyUpdateMessage()));
            this.session.send(new MessagePlayOutPlayerPositionAndLook(position.getX(), position.getY(), position.getZ(),
                    (float) rotation.getY(), (float) rotation.getX(), Collections.emptySet(), 0));
            this.setScoreboard(world.getScoreboard());
            this.inventoryContainer.openInventoryForAndInitialize(this);
            this.bossBars.forEach(bossBar -> bossBar.resendBossBar(this));
        } else {
            this.session.getServer().removePlayer(this);
            this.bossBars.forEach(bossBar -> bossBar.removeRawPlayer(this));
            this.tabList.clear();
            // Remove this player from the global tab list
            GlobalTabList.getInstance().get(this.gameProfile).ifPresent(GlobalTabListEntry::removeEntry);
        }
    }

    private static LanternTabListEntryBuilder createTabListEntryBuilder(LanternPlayer player) {
        return new LanternTabListEntryBuilder()
                .profile(player.getProfile())
                .displayName(Text.of(player.getName())) // TODO
                .gameMode(player.get(Keys.GAME_MODE).get())
                .latency(player.getConnection().getLatency());
    }

    private static final Set<RelativePositions> RELATIVE_ROTATION = Sets.immutableEnumSet(
            RelativePositions.PITCH, RelativePositions.YAW);
    private static final Set<RelativePositions> RELATIVE_POSITION = Sets.immutableEnumSet(
            RelativePositions.X, RelativePositions.Y, RelativePositions.Z);

    @Override
    public boolean setPositionAndWorld(World world, Vector3d position) {
        LanternWorld oldWorld = this.getWorld();
        boolean success = super.setPositionAndWorld(world, position);
        if (success && world == oldWorld) {
            this.session.send(new MessagePlayOutPlayerPositionAndLook(position.getX(), position.getY(), position.getZ(), 0, 0, RELATIVE_ROTATION, 0));
        }
        return success;
    }

    @Override
    public void setPosition(Vector3d position) {
        super.setPosition(position);
        LanternWorld world = this.getWorld();
        if (world != null) {
            this.session.send(new MessagePlayOutPlayerPositionAndLook(position.getX(), position.getY(), position.getZ(), 0, 0, RELATIVE_ROTATION, 0));
        }
    }

    @Override
    public void setRotation(Vector3d rotation) {
        super.setRotation(rotation);
        LanternWorld world = this.getWorld();
        if (world != null) {
            this.session.send(new MessagePlayOutPlayerPositionAndLook(0, 0, 0, (float) rotation.getX(), (float) rotation.getY(),
                    RELATIVE_POSITION, 0));
        }
    }

    public void setRawRotation(Vector3d rotation) {
        super.setRawRotation(rotation);
    }

    @Override
    public boolean setLocationAndRotation(Location<World> location, Vector3d rotation) {
        World oldWorld = this.getWorld();
        boolean success = super.setLocationAndRotation(location, rotation);
        if (success) {
            World world = location.getExtent();
            // Only send this if the world isn't changed, otherwise will the position be resend anyway
            if (oldWorld == world) {
                Vector3d pos = location.getPosition();
                MessagePlayOutPlayerPositionAndLook message = new MessagePlayOutPlayerPositionAndLook(pos.getX(), pos.getY(), pos.getZ(),
                        (float) rotation.getX(), (float) rotation.getY(), Collections.emptySet(), 0);
                this.session.send(message);
            }
        }
        return success;
    }

    @Override
    public boolean setLocationAndRotation(Location<World> location, Vector3d rotation, EnumSet<RelativePositions> relativePositions) {
        World oldWorld = this.getWorld();
        boolean success = super.setLocationAndRotation(location, rotation, relativePositions);
        if (success) {
            World world = location.getExtent();
            // Only send this if the world isn't changed, otherwise will the position be resend anyway
            if (oldWorld == world) {
                Vector3d pos = location.getPosition();
                MessagePlayOutPlayerPositionAndLook message = new MessagePlayOutPlayerPositionAndLook(pos.getX(), pos.getY(), pos.getZ(),
                        (float) rotation.getX(), (float) rotation.getY(), Sets.immutableEnumSet(relativePositions), 0);
                this.session.send(message);
            }
        }
        return success;
    }

    public void setRawPosition(Vector3d position) {
        super.setRawPosition(position);
    }

    @Override
    public void pulse() {
        // Check whether the player is still active
        int timeout = Lantern.getGame().getGlobalConfig().getPlayerIdleTimeout();
        if (timeout > 0 && System.currentTimeMillis() - this.lastActiveTime >= timeout * 60000) {
            this.session.disconnect(t("disconnect.idleTimeout"));
            return;
        }

        super.pulse();

        // TODO: Maybe async?
        this.pulseChunkChanges();

        // Pulse the interaction handler
        this.interactionHandler.pulse();

        // Stream the inventory updates
        this.inventoryContainer.streamSlotChanges();
        if (this.containerSession.getOpenContainer() != null) {
            this.containerSession.getOpenContainer().streamSlotChanges();
        }
    }

    /**
     * Gets the {@link ChunkLoadingTicket} that should be used
     * for this player.
     *
     * @return the chunk loading ticket
     */
    public ChunkLoadingTicket getChunkLoadingTicket() {
        // Allocate a new loading ticket, this can be null after
        // joining the server or switching worlds
        if (this.loadingTicket == null || ((ChunkLoadingTicket) this.loadingTicket).isReleased()) {
            this.loadingTicket = this.getWorld().getChunkManager().createPlayerEntityTicket(
                    Lantern.getMinecraftPlugin(), this.gameProfile.getUniqueId()).get();
            this.loadingTicket.bindToEntity(this);
        }
        return (ChunkLoadingTicket) this.loadingTicket;
    }

    public void pulseChunkChanges() {
        LanternWorld world = this.getWorld();
        if (world == null) {
            return;
        }

        ChunkLoadingTicket loadingTicket = this.getChunkLoadingTicket();
        Vector3d position = this.getPosition();

        double xPos = position.getX();
        double zPos = position.getZ();

        int centralX = ((int) xPos) >> 4;
        int centralZ = ((int) zPos) >> 4;

        // Fail fast if the player hasn't moved a chunk
        if (this.lastChunkPos != null && this.lastChunkPos.getX() == centralX &&
                this.lastChunkPos.getY() == centralZ) {
            return;
        }

        this.lastChunkPos = new Vector2i(centralX, centralZ);

        // Get the radius of visible chunks
        int radius = Math.min(world.getProperties().getConfig().getGeneration().getViewDistance(),
                this.viewDistance == -1 ? Integer.MAX_VALUE : this.viewDistance + 1);

        final Set<Vector2i> previousChunks = new HashSet<>(this.knownChunks);
        final List<Vector2i> newChunks = new ArrayList<>();

        for (int x = (centralX - radius); x <= (centralX + radius); x++) {
            for (int z = (centralZ - radius); z <= (centralZ + radius); z++) {
                final Vector2i coords = new Vector2i(x, z);
                if (!previousChunks.remove(coords)) {
                    newChunks.add(coords);
                }
            }
        }

        // Early end if there's no changes
        if (newChunks.size() == 0 && previousChunks.size() == 0) {
            return;
        }

        // Sort chunks by distance from player - closer chunks sent/forced first
        Collections.sort(newChunks, (a, b) -> {
            double dx = 16 * a.getX() + 8 - xPos;
            double dz = 16 * a.getY() + 8 - zPos;
            double da = dx * dx + dz * dz;
            dx = 16 * b.getX() + 8 - xPos;
            dz = 16 * b.getY() + 8 - zPos;
            double db = dx * dx + dz * dz;
            return Double.compare(da, db);
        });

        ObservedChunkManager observedChunkManager = world.getObservedChunkManager();

        // Force all the new chunks to be loaded and track the changes
        newChunks.forEach(coords -> {
            observedChunkManager.addObserver(coords, this);
            loadingTicket.forceChunk(coords);
        });
        // Unforce old chunks so they can unload and untrack the chunk
        previousChunks.forEach(coords -> {
            observedChunkManager.removeObserver(coords, this, true);
            loadingTicket.unforceChunk(coords);
        });

        this.knownChunks.removeAll(previousChunks);
        this.knownChunks.addAll(newChunks);
    }

    public User getUserObject() {
        return this.user;
    }

    @Override
    public void setInternalSubject(@Nullable Subject subject) {
        // We don't have to set the internal subject in the player instance
        // because it's already set in the user
    }

    @Override
    public Subject getInternalSubject() {
        return this.user.getInternalSubject();
    }

    @Override
    public String getSubjectCollectionIdentifier() {
        return this.user.getSubjectCollectionIdentifier();
    }

    @Override
    public Tristate getPermissionDefault(String permission) {
        return this.user.getPermissionDefault(permission);
    }

    @Override
    public boolean isOnline() {
        return this.session.getChannel().isActive();
    }

    @Override
    public Optional<Player> getPlayer() {
        return Optional.<Player>of(this);
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return Optional.of(this);
    }

    @Override
    public GameProfile getProfile() {
        return this.gameProfile;
    }

    @Override
    public String getIdentifier() {
        return this.getUniqueId().toString();
    }

    @Override
    public void sendMessage(ChatType type, Text message) {
        checkNotNull(message, "message");
        checkNotNull(type, "type");
        if (this.chatVisibility.isVisible(type)) {
            this.session.send(new MessagePlayOutChatMessage(new LocalizedText(message, this.locale), type));
        }
    }

    @Override
    public void sendMessage(Text message) {
        this.sendMessage(ChatTypes.CHAT, message);
    }

    @Override
    public MessageChannel getMessageChannel() {
        return this.messageChannel;
    }

    @Override
    public void setMessageChannel(MessageChannel channel) {
        this.messageChannel = checkNotNull(channel, "channel");
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position) {
        this.session.send(new MessagePlayOutParticleEffect(checkNotNull(position, "position"),
                checkNotNull(particleEffect, "particleEffect")));
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position, int radius) {
        checkNotNull(position, "position");
        checkNotNull(particleEffect, "particleEffect");
        if (this.getLocation().getPosition().distanceSquared(position) < radius * radius) {
            this.spawnParticles(particleEffect, position);
        }
    }

    @Override
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume, double pitch, double minVolume) {
        checkNotNull(sound, "sound");
        checkNotNull(position, "position");
        checkNotNull(category, "category");
        this.session.send(((LanternSoundType) sound).createMessage(position,
                category, (float) Math.max(minVolume, volume), (float) pitch));
    }

    @Override
    public void sendTitle(Title title) {
        this.session.send(LanternTitles.getMessages(checkNotNull(title, "title")));
    }

    @Override
    public void sendBookView(BookView bookView) {

    }

    @Override
    public void sendBlockChange(Vector3i position, BlockState state) {
        checkNotNull(state, "state");
        checkNotNull(position, "position");
        this.session.send(new MessagePlayOutBlockChange(position, BlockRegistryModule.get().getStateInternalIdAndData(state)));
    }

    @Override
    public void sendBlockChange(int x, int y, int z, BlockState state) {
        this.sendBlockChange(new Vector3i(x, y, z), state);
    }

    @Override
    public void resetBlockChange(Vector3i position) {
        checkNotNull(position, "position");
        LanternWorld world = this.getWorld();
        if (world == null) {
            return;
        }
        this.session.send(new MessagePlayOutBlockChange(position, BlockRegistryModule.get().getStateInternalIdAndData(world.getBlock(position))));
    }

    @Override
    public void resetBlockChange(int x, int y, int z) {
        this.resetBlockChange(new Vector3i(x, y, z));
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(Locale locale) {
        this.locale = checkNotNull(locale, "locale");
    }

    @Override
    public boolean isViewingInventory() {
        return false;
    }

    @Override
    public Optional<Inventory> getOpenInventory() {
        return null;
    }

    @Override
    public void openInventory(Inventory inventory, Cause cause) {

    }

    @Override
    public void closeInventory(Cause cause) {

    }

    @Override
    public int getViewDistance() {
        return this.viewDistance;
    }

    public void setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
    }

    @Override
    public ChatVisibility getChatVisibility() {
        return this.chatVisibility;
    }

    public void setChatVisibility(ChatVisibility chatVisibility) {
        this.chatVisibility = checkNotNull(chatVisibility, "chatVisibility");
    }

    @Override
    public boolean isChatColorsEnabled() {
        return this.chatColorsEnabled;
    }

    public void setChatColorsEnabled(boolean enabled) {
        this.chatColorsEnabled = enabled;
    }

    @Override
    public Set<SkinPart> getDisplayedSkinParts() {
        return this.skinParts;
    }

    public void setSkinParts(Set<SkinPart> skinParts) {
        this.skinParts = checkNotNull(skinParts, "skinParts");
    }

    public HandType getMainHand() {
        return this.mainHand;
    }

    public void setMainHand(HandType mainHand) {
        this.mainHand = checkNotNull(mainHand, "mainHand");
    }

    @Override
    public NetworkSession getConnection() {
        return this.session;
    }

    public Optional<ResourcePack> pollPendingResourcePackForStatus() {
        synchronized (this.pendingResourcePacksForStatus) {
            if (this.pendingResourcePacksForStatus.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(this.pendingResourcePacksForStatus.remove(0));
        }
    }

    @Override
    public void sendResourcePack(ResourcePack resourcePack) {
        checkNotNull(resourcePack, "resourcePack");
        final String hash = resourcePack.getHash().orElse(resourcePack.getId());
        final String location = resourcePack.getUri().toString();
        this.session.send(new MessagePlayOutSendResourcePack(location, hash));
        synchronized (this.pendingResourcePacksForStatus) {
            this.pendingResourcePacksForStatus.add(resourcePack);
        }
    }

    @Override
    public LanternTabList getTabList() {
        return this.tabList;
    }

    @Override
    public void kick() {
        this.session.disconnect();
    }

    @Override
    public void kick(Text reason) {
        this.session.disconnect(reason);
    }

    @Override
    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) {
        checkNotNull(scoreboard, "scoreboard");
        if (this.scoreboard != null && scoreboard != this.scoreboard) {
            this.scoreboard.removePlayer(this);
        }
        this.scoreboard = (LanternScoreboard) scoreboard;
        this.scoreboard.addPlayer(this);
    }

    @Override
    public Text getTeamRepresentation() {
        return Text.of(this.getName());
    }

    @Override
    public boolean isSleepingIgnored() {
        return this.sleepingIgnored;
    }

    @Override
    public void setSleepingIgnored(boolean sleepingIgnored) {
        this.sleepingIgnored = sleepingIgnored;
    }

    public PlayerInteractionHandler getInteractionHandler() {
        return this.interactionHandler;
    }

    @Override
    public LanternHumanInventory getInventory() {
        return this.inventory;
    }

    /**
     * Gets the {@link PlayerContainerSession}.
     *
     * @return The container session
     */
    public PlayerContainerSession getContainerSession() {
        return this.containerSession;
    }

    public HumanInventoryContainer getInventoryContainer() {
        return this.inventoryContainer;
    }
}

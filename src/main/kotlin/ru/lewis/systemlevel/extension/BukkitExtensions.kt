package ru.lewis.systemlevel.extension

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import de.tr7zw.changeme.nbtapi.NBT
import me.lucko.helper.Schedulers
import me.lucko.helper.promise.Promise
import me.lucko.helper.serialize.BlockPosition
import me.lucko.helper.serialize.BlockRegion
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.util.BoundingBox
import ru.lewis.systemlevel.model.ExtendedAudience
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


fun Array<out BaseComponent>.toAdventure(): Component {
    return BungeeComponentSerializer.get().deserialize(this).compact()
}

fun Color.toAwtColor(): java.awt.Color {
    return java.awt.Color(this.red, this.green, this.blue)
}

fun World.getHighestBlockAt(x: Int, z: Int, filter: (Block) -> Boolean): Block? {
    syncOnly()

    return (minHeight..maxHeight).reversed().asSequence()
        .map { getBlockAt(x, it, z) }
        .firstOrNull(filter)
}

fun ItemStack.setSkullTexture(texture: String) {
    NBT.modify(this) {
        it.getOrCreateCompound("SkullOwner")
            .apply { setUUID("Id", UUID.randomUUID()) }
            .getOrCreateCompound("Properties")
            .getCompoundList("textures")
            .addCompound()
            .setString("Value", texture)
    }
}

@OptIn(ExperimentalContracts::class)
inline fun Player.forceTeleportAsync(
    location: Location,
    ifNotTeleported: () -> Unit = {
        Schedulers.sync().run { this.adventure.kick("<red>Вы должны были телепортироваться".parseMiniMessage()) }
            .join()
    }
): Boolean {
    contract {
        callsInPlace(ifNotTeleported, InvocationKind.AT_MOST_ONCE)
    }

    require(!Bukkit.isPrimaryThread()) { "Call this async!" }

    if (!this.teleportAsyncPromise(location).join()) {
        ifNotTeleported()
        return false
    }

    return true
}

@OptIn(ExperimentalContracts::class)
inline fun Player.forceTeleport(
    location: Location,
    ifNotTeleported: () -> Unit = {
        runSync { this.adventure.kick("<red>Вы должны были телепортироваться".parseMiniMessage()) }.join()
    }
): Boolean {
    contract {
        callsInPlace(ifNotTeleported, InvocationKind.AT_MOST_ONCE)
    }

    if (Bukkit.isPrimaryThread()) {
        if (!this.teleport(location)) {
            ifNotTeleported()
            return false
        }

        return true
    } else {
        return forceTeleportAsync(location, ifNotTeleported)
    }
}

fun Player.teleportAsyncPromise(loc: Location): Promise<Boolean> {
    return Promise.supplyingAsync {
        Promise.supplyingSync { this.teleportAsync(loc) }
            .join()
            .join()
    }
}

fun BoundingBox.toBlockRegion(world: World): BlockRegion = BlockRegion.of(
    BlockPosition.of(this.min.toLocation(world)),
    BlockPosition.of(this.max.toLocation(world)),
)


val CommandSender.adventure: ExtendedAudience
    get() {
        return ExtendedAudience(listOf(this))
    }

val Iterable<CommandSender>.adventure: ExtendedAudience
    get() {
        return ExtendedAudience(this)
    }

val Sequence<CommandSender>.adventure: ExtendedAudience
    get() {
        return ExtendedAudience(this.asIterable())
    }

@Suppress("RemoveRedundantQualifierName")
fun org.bukkit.event.block.Action.isRightClick(): Boolean {
    return this == Action.RIGHT_CLICK_AIR || this == Action.RIGHT_CLICK_BLOCK
}

fun syncOnly() {
    require(Bukkit.isPrimaryThread()) { "This block of code must be only executed synchronously" }
}

fun asyncOnly() {
    if (!Bukkit.isStopping()) require(!Bukkit.isPrimaryThread()) { "This block of code must be only executed asynchronously" }
}

fun Map<Int, ItemStack>.serializeToNBT(): String {
    return ObjectMapper().writeValueAsString(this.mapValues { entry -> NBT.itemStackToNBT(entry.value).toString() })
}

fun String.deserializeItems(): Map<Int, ItemStack> {
    val type = object : TypeReference<MutableMap<Int, String>>() {}
    return ObjectMapper()
        .createParser(this).readValueAs<MutableMap<Int, String>>(type)
        .asSequence()
        .mapNotNull { NBT.itemStackFromNBT(NBT.parseNBT(it.value))?.let { item -> it.key to item } }
        .toMap()
}

fun Player.togglePlayerVisibility(plugin: Plugin, target: Player, state: Boolean) {
    runSync {
        if (state) showPlayer(plugin, target) else hidePlayer(plugin, target)
    }
}

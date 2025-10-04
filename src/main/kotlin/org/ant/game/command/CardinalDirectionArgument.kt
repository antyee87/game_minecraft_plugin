package org.ant.game.command

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.kyori.adventure.text.Component
import org.ant.game.gameimpl.gameframe.GameConstants
import java.util.concurrent.CompletableFuture

class CardinalDirectionArgument : CustomArgumentType.Converted<GameConstants.CardinalDirection, String> {
    companion object {
        val ERROR_INVALID_VALUE = DynamicCommandExceptionType { value: Any? ->
            MessageComponentSerializer.message()
                .serialize(Component.text("$value is not a valid cardinal direction!"))
        }
    }

    override fun convert(nativeType: String): GameConstants.CardinalDirection {
        try {
            return GameConstants.CardinalDirection.valueOf(nativeType.uppercase())
        } catch (ignored: IllegalArgumentException) {
            throw ERROR_INVALID_VALUE.create(nativeType)
        }
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        for (cardinalDirection in GameConstants.CardinalDirection.entries) {
            val name = cardinalDirection.name.lowercase()
            if (name.startsWith(builder.remainingLowerCase)) {
                builder.suggest(name)
            }
        }
        return builder.buildFuture()
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.word()
    }
}

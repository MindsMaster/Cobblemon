import com.cobblemon.mod.common.CobblemonItemComponents
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import java.util.Optional

class PotComponent(val potItem: ItemStack) { // No nullable ItemStack

    companion object {
        // Codec for saving/loading
        val CODEC: Codec<PotComponent> = RecordCodecBuilder.create { instance ->
            instance.group(
                ItemStack.CODEC.fieldOf("potItem").forGetter { it.potItem }
            ).apply(instance) { potItem -> PotComponent(potItem) }
        }

        // StreamCodec for network synchronization
        val PACKET_CODEC: StreamCodec<ByteBuf, PotComponent> = ByteBufCodecs.fromCodec(CODEC)

        // Fetch component from stack
        fun getFrom(stack: ItemStack): PotComponent? {
            return stack.components.get(CobblemonItemComponents.POT_DATA)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is PotComponent && ItemStack.matches(this.potItem, other.potItem)
    }

    override fun hashCode(): Int {
        return ItemStack.hashItemAndComponents(potItem)
    }
}
import com.cobblemon.mod.common.CobblemonItemComponents
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import java.util.Optional

class PotComponent(val potItem: ItemStack?) {

    companion object {
        // Codec for saving/loading
        val CODEC: Codec<PotComponent> = RecordCodecBuilder.create { instance ->
            instance.group(
                ItemStack.CODEC.optionalFieldOf("potItem").forGetter { Optional.ofNullable(it.potItem) } // Convert nullable to Optional
            ).apply(instance) { optionalPotItem ->
                PotComponent(optionalPotItem.orElse(null)) // Convert Optional back to nullable
            }
        }

        // StreamCodec for network synchronization
        val PACKET_CODEC: StreamCodec<ByteBuf, PotComponent> = ByteBufCodecs.fromCodec(CODEC)

        // Helper for fetching the component
        fun getFrom(stack: ItemStack): PotComponent? {
            return stack.components.get(CobblemonItemComponents.POT_ITEM)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is PotComponent && this.potItem == other.potItem
    }

    override fun hashCode(): Int {
        return potItem?.hashCode() ?: 0
    }
}

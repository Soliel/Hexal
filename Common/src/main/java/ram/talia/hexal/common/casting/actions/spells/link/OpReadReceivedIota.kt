package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.linkable.LinkableRegistry

object OpReadReceivedIota : ConstMediaAction {
	override val argc = 0

	override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
		return listOf(LinkableRegistry.linkableFromCastingContext(ctx).nextReceivedIota())
	}
}
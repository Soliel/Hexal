package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Pose
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.nextColour
import ram.talia.hexal.client.playLinkParticles
import ram.talia.hexal.client.sounds.WispCastingSoundInstance
import ram.talia.hexal.common.lib.HexalSounds
import ram.talia.hexal.common.network.MsgWispCastSoundAck
import ram.talia.hexal.xplat.IXplatAbstractions
import kotlin.math.*

abstract class BaseWisp(entityType: EntityType<out BaseWisp>, world: Level)  : LinkableEntity(entityType, world), IMediaEntity<BaseWisp> {
	var oldPos: Vec3 = position()

	var soundInstance: WispCastingSoundInstance? = null

	override var media: Int
		get() = entityData.get(MEDIA)
		set(value) = entityData.set(MEDIA, max(value, 0))

	override val isConsumable = true

	override fun get() = this

	override fun colouriser() = FrozenColorizer.fromNBT(entityData.get(COLOURISER))

	override fun getEyeHeight(pose: Pose, dim: EntityDimensions) = 0f

	override fun makeBoundingBox(): AABB {
		return super.makeBoundingBox().move(0.0, -getDimensions(Pose.STANDING).height*0.5, 0.0)
	}

	/**
	 * Set the look vector of the wisp equal a given vector
	 */
	fun setLookVector(vel: Vec3) {
		val direction = vel.normalize()

		val pitch = asin(direction.y)
		val yaw = asin(max(min(direction.x / cos(pitch), 1.0), -1.0))

		xRot = (-pitch * 180 / Math.PI).toFloat()
		yRot = (-yaw * 180 / Math.PI).toFloat()
		xRotO = xRot
		yRotO = yRot
	}

	/**
	 * Returns the maximum length vector that the wisp can move (up to the length of [step], and along the line of [step]) before it collides with something.
	 */
	fun maxMove(step: Vec3): Vec3 {
		val bBox = this.boundingBox
		val voxelShapes = level.getEntityCollisions(this, bBox.expandTowards(deltaMovement))
		return if (step.lengthSqr() == 0.0) step else collideBoundingBox(this, step, bBox, level, voxelShapes)
	}

	fun scheduleCastSound() {
		if (level.isClientSide)
			throw Exception("BaseWisp.scheduleCastSound should only be called on server.") // TODO: create and replace with ServerOnlyException
		HexalAPI.LOGGER.info("scheduling casting sound, level is $level")
		IXplatAbstractions.INSTANCE.sendPacketNear(position(), 32.0, level as ServerLevel, MsgWispCastSoundAck(this))
	}

	fun playCastSoundClient() {
		if (!level.isClientSide)
			throw Exception("BaseWisp.playCastSoundClient should only be called on client.") // TODO: create and replace with ClientOnlyException

		HexalAPI.LOGGER.info("playing casting sound, level is $level")
		if (soundInstance == null || soundInstance!!.isStopped) {
			soundInstance = WispCastingSoundInstance(this)
			Minecraft.getInstance().soundManager.play(soundInstance!!)
			HexalSounds.WISP_CASTING_START.playAt(level, position(), .3f, 1f + (random.nextFloat() - 0.5f) * 0.2f, false)
		}

		soundInstance!!.keepAlive()
	}

	fun renderCentre(): Vec3 = position()
	override fun renderCentre(other: ILinkable.IRenderCentre, recursioning: Boolean): Vec3 = renderCentre()

	fun playTrailParticles() {
		playTrailParticles(colouriser())
	}

	protected open fun playWispParticles(colouriser: FrozenColorizer) {
		val radius = (media.toDouble() / ManaConstants.DUST_UNIT).pow(1.0 / 3) / 100

		for (i in 0..50) {
			val colour: Int = colouriser.nextColour(random)

			level.addParticle(
				ConjureParticleOptions(colour, true),
				(renderCentre().x + radius*random.nextGaussian()),
				(renderCentre().y + radius*random.nextGaussian()),
				(renderCentre().z + radius*random.nextGaussian()),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5)
			)
		}
	}

	protected open fun playTrailParticles(colouriser: FrozenColorizer) {
		val radius = ceil((media.toDouble() / ManaConstants.DUST_UNIT).pow(1.0 / 3) / 10)

		val delta = oldPos - position()
		val dist = delta.length() * 12 * radius * radius * radius

		for (i in 0..dist.toInt()) {
			val colour: Int = colouriser.nextColour(random)

			val coeff = i / dist
			level.addParticle(
				ConjureParticleOptions(colour, false),
				(renderCentre().x + delta.x * coeff),
				(renderCentre().y + delta.y * coeff),
				(renderCentre().z + delta.z * coeff),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5)
			)
		}
	}

	fun playAllLinkParticles() {
		renderLinks.forEach { playLinkParticles(this, it, random, level) }
	}

	fun setColouriser(colouriser: FrozenColorizer) {
		entityData.set(COLOURISER, colouriser.serializeToNBT())
	}

	override fun readAdditionalSaveData(compound: CompoundTag) {
		super.readAdditionalSaveData(compound)

		entityData.set(COLOURISER, compound.getCompound(TAG_COLOURISER))

		media = compound.getInt(TAG_MEDIA)
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		super.addAdditionalSaveData(compound)

		compound.putCompound(TAG_COLOURISER, entityData.get(COLOURISER))
		compound.putInt(TAG_MEDIA, media)
	}

	override fun defineSynchedData() {
		super.defineSynchedData()

		entityData.define(COLOURISER, FrozenColorizer.DEFAULT.get().serializeToNBT())
		entityData.define(MEDIA, 20 * ManaConstants.DUST_UNIT)
	}

	override fun getAddEntityPacket(): Packet<*> {
		return ClientboundAddEntityPacket(this, 0)
	}

	companion object {
		@JvmStatic
		val COLOURISER: EntityDataAccessor<CompoundTag> = SynchedEntityData.defineId(BaseWisp::class.java, EntityDataSerializers.COMPOUND_TAG)

		@JvmStatic
		val MEDIA: EntityDataAccessor<Int> = SynchedEntityData.defineId(BaseWisp::class.java, EntityDataSerializers.INT)

		const val TAG_COLOURISER = "colouriser"
		const val TAG_MEDIA = "media"
	}
}
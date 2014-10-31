package WayofTime.alchemicalWizardry.common.demonVillage.demonHoard.demon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.world.World;
import WayofTime.alchemicalWizardry.ModItems;
import WayofTime.alchemicalWizardry.common.EntityAITargetAggro;
import WayofTime.alchemicalWizardry.common.Int3;
import WayofTime.alchemicalWizardry.common.entity.mob.EntityDemon;
import WayofTime.alchemicalWizardry.common.entity.projectile.HolyProjectile;
import WayofTime.alchemicalWizardry.common.spell.complex.effect.SpellHelper;

public class EntityMinorDemonGrunt extends EntityDemon implements IRangedAttackMob, IHoardDemon
{
    private EntityAIArrowAttack aiArrowAttack = new EntityAIArrowAttack(this, 1.0D, 40, 40, 15.0F);
    private EntityAIAttackOnCollide aiAttackOnCollide = new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.2D, false);

    private boolean isAngry = false;
    private Int3 demonPortal;
    
    private static float maxTamedHealth = 50.0F;
    private static float maxUntamedHealth = 50.0F;

    public EntityMinorDemonGrunt(World par1World)
    {
        super(par1World, "GruntString");
        this.setSize(0.7F, 1.8F);
        this.getNavigator().setAvoidsWater(true);
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, this.aiSit);
        this.tasks.addTask(3, new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(4, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(6, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(4, new EntityAITargetAggro(this, EntityPlayer.class, 0, false));
        this.setAggro(false);
        this.setTamed(false);
        
        demonPortal = new Int3(0,0,0);

        if (par1World != null && !par1World.isRemote)
        {
            this.setCombatTask();
        }

        //this.isImmuneToFire = true;
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        //This line affects the speed of the monster
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.30000001192092896D);

        //My guess is that this will alter the max health
        if (this.isTamed())
        {
            this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(this.maxTamedHealth);
        } else
        {
            this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(this.maxUntamedHealth);
        }
    }
    
    @Override
    public void setPortalLocation(Int3 position)
    {
    	this.demonPortal = position;
    }
    
    @Override
    public Int3 getPortalLocation()
    {
    	return this.demonPortal;
    }

    /**
     * Returns true if the newer Entity AI code should be run
     */
    public boolean isAIEnabled()
    {
        return true;
    }

    /**
     * Sets the active target the Task system uses for tracking
     */
    public void setAttackTarget(EntityLivingBase par1EntityLivingBase)
    {
        super.setAttackTarget(par1EntityLivingBase);

        if (par1EntityLivingBase == null)
        {
            this.setAngry(false);
        } else if (!this.isTamed())
        {
            this.setAngry(true);
        }
    }

    /**
     * main AI tick function, replaces updateEntityActionState
     */
    protected void updateAITick()
    {
        this.dataWatcher.updateObject(18, Float.valueOf(this.getHealth()));
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setBoolean("Angry", this.isAngry());
        
        this.demonPortal.writeToNBT(par1NBTTagCompound);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);
        this.setAngry(par1NBTTagCompound.getBoolean("Angry"));
        this.demonPortal = Int3.readFromNBT(par1NBTTagCompound);
        
        this.setCombatTask();
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    @Override
    protected String getLivingSound()
    {
        //TODO change sounds
        return "none";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    @Override
    protected String getHurtSound()
    {
        return "none";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    @Override
    protected String getDeathSound()
    {
        return "mob.wolf.death";
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    @Override
    protected float getSoundVolume()
    {
        return 0.4F;
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    @Override
    public void onLivingUpdate()
    {
        super.onLivingUpdate();
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate()
    {
        super.onUpdate();
    }

    @Override
    public float getEyeHeight()
    {
        return this.height * 0.8F;
    }

    /**
     * The speed it takes to move the entityliving's rotationPitch through the faceEntity method. This is only currently
     * use in wolves.
     */
    public int getVerticalFaceSpeed()
    {
        return this.isSitting() ? 20 : super.getVerticalFaceSpeed();
    }

    public void setTamed(boolean par1)
    {
        super.setTamed(par1);

        if (par1)
        {
            this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(this.maxTamedHealth);
        } else
        {
            this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(this.maxUntamedHealth);
        }
    }

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    @Override
    public boolean interact(EntityPlayer par1EntityPlayer)
    {
        ItemStack itemstack = par1EntityPlayer.inventory.getCurrentItem();

        if (this.isTamed())
        {
            if (itemstack != null)
            {
                if (itemstack.getItem() instanceof ItemFood)
                {
                    ItemFood itemfood = (ItemFood) itemstack.getItem();

                    if (itemfood.isWolfsFavoriteMeat() && this.dataWatcher.getWatchableObjectFloat(18) < this.maxTamedHealth)
                    {
                        if (!par1EntityPlayer.capabilities.isCreativeMode)
                        {
                            --itemstack.stackSize;
                        }

                        this.heal((float) itemfood.func_150905_g(itemstack));

                        if (itemstack.stackSize <= 0)
                        {
                            par1EntityPlayer.inventory.setInventorySlotContents(par1EntityPlayer.inventory.currentItem, (ItemStack) null);
                        }

                        return true;
                    }
                }
            }

            if (this.getOwner() instanceof EntityPlayer && SpellHelper.getUsername(par1EntityPlayer).equalsIgnoreCase(SpellHelper.getUsername((EntityPlayer) this.getOwner())) && !this.isBreedingItem(itemstack))
            {
                if (!this.worldObj.isRemote)
                {
                    this.aiSit.setSitting(!this.isSitting());
                    this.isJumping = false;
                    this.setPathToEntity((PathEntity) null);
                    this.setTarget((Entity) null);
                    this.setAttackTarget((EntityLivingBase) null);
                }

                this.sendSittingMessageToPlayer(par1EntityPlayer, !this.isSitting());
            }
        } else if (itemstack != null && itemstack.getItem().equals(ModItems.weakBloodOrb) && !this.isAngry())
        {
            if (!par1EntityPlayer.capabilities.isCreativeMode)
            {
                --itemstack.stackSize;
            }

            if (itemstack.stackSize <= 0)
            {
                par1EntityPlayer.inventory.setInventorySlotContents(par1EntityPlayer.inventory.currentItem, null);
            }

            if (!this.worldObj.isRemote)
            {
                if (this.rand.nextInt(1) == 0)
                {
                    this.setTamed(true);
                    this.setPathToEntity((PathEntity) null);
                    this.setAttackTarget((EntityLivingBase) null);
                    this.aiSit.setSitting(true);
                    this.setHealth(this.maxTamedHealth);
                    this.func_152115_b(par1EntityPlayer.getUniqueID().toString());
                    this.playTameEffect(true);
                    this.worldObj.setEntityState(this, (byte) 7);
                } else
                {
                    this.playTameEffect(false);
                    this.worldObj.setEntityState(this, (byte) 6);
                }
            }

            return true;
        }

        return super.interact(par1EntityPlayer);
    }


    public boolean isBreedingItem(ItemStack par1ItemStack)
    {
        return false;
    }

    /**
     * Determines whether this wolf is angry or not.
     */
    public boolean isAngry()
    {
        return this.isAngry;
    }

    /**
     * Sets whether this wolf is angry or not.
     */
    public void setAngry(boolean angry)
    {
        this.isAngry = angry;
    }


    /**
     * Returns true if the mob is currently able to mate with the specified mob.
     */
    @Override
    public boolean canMateWith(EntityAnimal par1EntityAnimal)
    {
        return false;
    }


    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    @Override
    protected boolean canDespawn()
    {
        //return !this.isTamed() && this.ticksExisted > 2400;
        return false;
    }

    /**
     * A call to determine if this entity should attack the other entity
     */
    @Override
    public boolean func_142018_a(EntityLivingBase par1EntityLivingBase, EntityLivingBase par2EntityLivingBase)
    {
        if (!(par1EntityLivingBase instanceof EntityCreeper) && !(par1EntityLivingBase instanceof EntityGhast))
        {
            if (par1EntityLivingBase instanceof EntityDemon)
            {
                EntityDemon entitywolf = (EntityDemon) par1EntityLivingBase;

                if (entitywolf.isTamed() && entitywolf.getOwner() == par2EntityLivingBase)
                {
                    return false;
                }
            }

            return par1EntityLivingBase instanceof EntityPlayer && par2EntityLivingBase instanceof EntityPlayer && !((EntityPlayer) par2EntityLivingBase).canAttackPlayer((EntityPlayer) par1EntityLivingBase) ? false : !(par1EntityLivingBase instanceof EntityHorse) || !((EntityHorse) par1EntityLivingBase).isTame();
        } else
        {
            return false;
        }
    }

    /**
     * Attack the specified entity using a ranged attack.
     */
    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase par1EntityLivingBase, float par2)
    {
        double xCoord;
        double yCoord;
        double zCoord;
        HolyProjectile hol = new HolyProjectile(worldObj, this, par1EntityLivingBase, 1.8f, 0f, 5, 600);
        this.worldObj.spawnEntityInWorld(hol);
    }

    /**
     * sets this entity's combat AI.
     */
    public void setCombatTask()
    {
        this.tasks.removeTask(this.aiAttackOnCollide);
        this.tasks.removeTask(this.aiArrowAttack);
        this.tasks.addTask(4, this.aiArrowAttack);
    }
}

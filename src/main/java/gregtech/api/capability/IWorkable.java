package gregtech.api.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * For machines which have progress and can work
 */
public interface IWorkable {

    @CapabilityInject(IWorkable.class)
    public static final Capability<IWorkable> CAPABILITY_WORKABLE = null;

    /**
     * @return current progress of machine
     */
    int getProgress();

    /**
     * @return progress machine need to complete it's stuff
     */
    int getMaxProgress();

    /**
     * Increases current progress of machine
     */
    void increaseProgress(int progress);

    /**
     * @return true if machine has some action in queue now (even if it is disallowed to work or inactive)
     */
    boolean hasWorkToDo();

    /**
     * Allows machine to work
     * Machine will continue work from progress it got when disableWorking() was called
     */
    void enableWorking();

    /**
     * Disallows machine to work
     * Disallowing work will stop machine from continuing progress
     */
    void disableWorking();

    /**
     * @return true if machine is allowed to work
     */
    boolean isAllowedToWork();

    /**
     * @return true is machine is active
     */
    boolean isActive();

    /**
     * Sets the active status of the machine
     * Setting machine inactive will NOT reset current progress
     */
    void setActive(boolean active);

}
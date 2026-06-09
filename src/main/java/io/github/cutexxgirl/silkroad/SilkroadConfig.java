package io.github.cutexxgirl.silkroad;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedList;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import net.minecraft.resources.ResourceLocation;

public final class SilkroadConfig extends Config {
    private static transient SilkroadConfig instance = new SilkroadConfig();

    public static transient ValidatedBoolean ENABLED = instance.general.enabled;
    public static transient ValidatedBoolean FIRST_PERSON_ROTATION_ENABLED = instance.firstPerson.rotationEnabled;
    public static transient ValidatedBoolean FIRST_PERSON_VERTICAL_ENABLED = instance.firstPerson.verticalEnabled;
    public static transient ValidatedBoolean THIRD_PERSON_ENABLED = instance.thirdPerson.positionLagEnabled;
    public static transient ValidatedBoolean THIRD_PERSON_ROTATION_ENABLED = instance.thirdPerson.rotationEnabled;
    public static transient ValidatedBoolean DISABLE_WHILE_AIMING = instance.aiming.disableWhileAiming;
    public static transient ValidatedDouble AIM_BLEND_OUT_SPEED = instance.aiming.blendOutSpeed;
    public static transient ValidatedDouble FIRST_PERSON_ROTATION_FREQUENCY = instance.advanced.firstPersonRotationFrequency;
    public static transient ValidatedDouble FIRST_PERSON_ROTATION_DAMPING = instance.advanced.firstPersonRotationDamping;
    public static transient ValidatedDouble FIRST_PERSON_VERTICAL_RESPONSE = instance.advanced.firstPersonVerticalResponse;
    public static transient ValidatedDouble FIRST_PERSON_VERTICAL_SNAP_THRESHOLD = instance.advanced.firstPersonVerticalSnapThreshold;
    public static transient ValidatedDouble FIRST_PERSON_VERTICAL_FREQUENCY = instance.advanced.firstPersonVerticalFrequency;
    public static transient ValidatedDouble FIRST_PERSON_VERTICAL_DAMPING = instance.advanced.firstPersonVerticalDamping;
    public static transient ValidatedDouble THIRD_PERSON_POSITION_FREQUENCY = instance.thirdPerson.positionFrequency;
    public static transient ValidatedDouble THIRD_PERSON_POSITION_DAMPING = instance.advanced.thirdPersonPositionDamping;
    public static transient ValidatedDouble THIRD_PERSON_VERTICAL_RESPONSE = instance.advanced.thirdPersonVerticalResponse;
    public static transient ValidatedDouble THIRD_PERSON_VERTICAL_SNAP_THRESHOLD = instance.advanced.thirdPersonVerticalSnapThreshold;
    public static transient ValidatedDouble THIRD_PERSON_ROTATION_FREQUENCY = instance.advanced.thirdPersonRotationFrequency;
    public static transient ValidatedDouble THIRD_PERSON_ROTATION_DAMPING = instance.advanced.thirdPersonRotationDamping;
    public static transient ValidatedBoolean THIRD_PERSON_STEP_UP_SMOOTHING_ENABLED = instance.thirdPerson.stepUpSmoothingEnabled;
    public static transient ValidatedDouble THIRD_PERSON_STEP_UP_MIN_HEIGHT = instance.advanced.thirdPersonStepUpMinHeight;
    public static transient ValidatedDouble THIRD_PERSON_STEP_UP_MAX_HEIGHT = instance.advanced.thirdPersonStepUpMaxHeight;
    public static transient ValidatedDouble THIRD_PERSON_STEP_UP_RESPONSE = instance.advanced.thirdPersonStepUpResponse;
    public static transient ValidatedDouble THIRD_PERSON_STEP_UP_SNAP_THRESHOLD = instance.advanced.thirdPersonStepUpSnapThreshold;
    public static transient ValidatedDouble THIRD_PERSON_STEP_UP_RESET_DISTANCE = instance.advanced.thirdPersonStepUpResetDistance;
    public static transient ValidatedBoolean EXPERIMENTAL_THIRD_PERSON_RIG_ENABLED = instance.experimental.thirdPersonRigEnabled;
    public static transient ValidatedDouble THIRD_PERSON_RIG_MIN_DISTANCE = instance.experimental.thirdPersonRigMinDistance;
    public static transient ValidatedDouble THIRD_PERSON_RIG_MAX_DISTANCE = instance.experimental.thirdPersonRigMaxDistance;
    public static transient ValidatedDouble THIRD_PERSON_RIG_COLLISION_PADDING = instance.experimental.thirdPersonRigCollisionPadding;
    public static transient ValidatedDouble MAX_LAG_DISTANCE = instance.general.maxLagDistance;
    public static transient ValidatedBoolean IGNORE_CROUCH_HEIGHT_IN_THIRD_PERSON = instance.thirdPerson.ignoreCrouchHeight;
    public static transient ValidatedBoolean PEHKUI_COMPAT_ENABLED = instance.compatibility.pehkuiEnabled;
    public static transient ValidatedDouble RESET_DISTANCE = instance.advanced.resetDistance;
    public static transient ValidatedList<String> AIMING_ITEM_NAMESPACES = instance.aiming.itemNamespaces;
    public static transient ValidatedList<String> AIMING_ITEM_IDS = instance.aiming.itemIds;

    public General general = new General();
    public FirstPerson firstPerson = new FirstPerson();
    public ThirdPerson thirdPerson = new ThirdPerson();
    public Aiming aiming = new Aiming();
    public Compatibility compatibility = new Compatibility();
    public Advanced advanced = new Advanced();
    public Experimental experimental = new Experimental();

    public SilkroadConfig() {
        super(ResourceLocation.fromNamespaceAndPath(Silkroad.MOD_ID, "client"));
    }

    public static void register() {
        instance = ConfigApiJava.registerAndLoadConfig(SilkroadConfig::new, RegisterType.CLIENT);
        bindStaticFields();
    }

    private static void bindStaticFields() {
        ENABLED = instance.general.enabled;
        FIRST_PERSON_ROTATION_ENABLED = instance.firstPerson.rotationEnabled;
        FIRST_PERSON_VERTICAL_ENABLED = instance.firstPerson.verticalEnabled;
        THIRD_PERSON_ENABLED = instance.thirdPerson.positionLagEnabled;
        THIRD_PERSON_ROTATION_ENABLED = instance.thirdPerson.rotationEnabled;
        DISABLE_WHILE_AIMING = instance.aiming.disableWhileAiming;
        AIM_BLEND_OUT_SPEED = instance.aiming.blendOutSpeed;
        FIRST_PERSON_ROTATION_FREQUENCY = instance.advanced.firstPersonRotationFrequency;
        FIRST_PERSON_ROTATION_DAMPING = instance.advanced.firstPersonRotationDamping;
        FIRST_PERSON_VERTICAL_RESPONSE = instance.advanced.firstPersonVerticalResponse;
        FIRST_PERSON_VERTICAL_SNAP_THRESHOLD = instance.advanced.firstPersonVerticalSnapThreshold;
        FIRST_PERSON_VERTICAL_FREQUENCY = instance.advanced.firstPersonVerticalFrequency;
        FIRST_PERSON_VERTICAL_DAMPING = instance.advanced.firstPersonVerticalDamping;
        THIRD_PERSON_POSITION_FREQUENCY = instance.thirdPerson.positionFrequency;
        THIRD_PERSON_POSITION_DAMPING = instance.advanced.thirdPersonPositionDamping;
        THIRD_PERSON_VERTICAL_RESPONSE = instance.advanced.thirdPersonVerticalResponse;
        THIRD_PERSON_VERTICAL_SNAP_THRESHOLD = instance.advanced.thirdPersonVerticalSnapThreshold;
        THIRD_PERSON_ROTATION_FREQUENCY = instance.advanced.thirdPersonRotationFrequency;
        THIRD_PERSON_ROTATION_DAMPING = instance.advanced.thirdPersonRotationDamping;
        THIRD_PERSON_STEP_UP_SMOOTHING_ENABLED = instance.thirdPerson.stepUpSmoothingEnabled;
        THIRD_PERSON_STEP_UP_MIN_HEIGHT = instance.advanced.thirdPersonStepUpMinHeight;
        THIRD_PERSON_STEP_UP_MAX_HEIGHT = instance.advanced.thirdPersonStepUpMaxHeight;
        THIRD_PERSON_STEP_UP_RESPONSE = instance.advanced.thirdPersonStepUpResponse;
        THIRD_PERSON_STEP_UP_SNAP_THRESHOLD = instance.advanced.thirdPersonStepUpSnapThreshold;
        THIRD_PERSON_STEP_UP_RESET_DISTANCE = instance.advanced.thirdPersonStepUpResetDistance;
        EXPERIMENTAL_THIRD_PERSON_RIG_ENABLED = instance.experimental.thirdPersonRigEnabled;
        THIRD_PERSON_RIG_MIN_DISTANCE = instance.experimental.thirdPersonRigMinDistance;
        THIRD_PERSON_RIG_MAX_DISTANCE = instance.experimental.thirdPersonRigMaxDistance;
        THIRD_PERSON_RIG_COLLISION_PADDING = instance.experimental.thirdPersonRigCollisionPadding;
        MAX_LAG_DISTANCE = instance.general.maxLagDistance;
        IGNORE_CROUCH_HEIGHT_IN_THIRD_PERSON = instance.thirdPerson.ignoreCrouchHeight;
        PEHKUI_COMPAT_ENABLED = instance.compatibility.pehkuiEnabled;
        RESET_DISTANCE = instance.advanced.resetDistance;
        AIMING_ITEM_NAMESPACES = instance.aiming.itemNamespaces;
        AIMING_ITEM_IDS = instance.aiming.itemIds;
    }

    public static final class General extends ConfigSection {
        public ValidatedBoolean enabled = new ValidatedBoolean(true);
        public ValidatedDouble maxLagDistance = new ValidatedDouble(2.5D, 16.0D, 0.0D);
    }

    public static final class FirstPerson extends ConfigSection {
        public ValidatedBoolean rotationEnabled = new ValidatedBoolean(true);
        public ValidatedBoolean verticalEnabled = new ValidatedBoolean(true);
    }

    public static final class ThirdPerson extends ConfigSection {
        public ValidatedBoolean positionLagEnabled = new ValidatedBoolean(true);
        public ValidatedDouble positionFrequency = new ValidatedDouble(3.6D, 30.0D, 0.1D);
        public ValidatedBoolean rotationEnabled = new ValidatedBoolean(true);
        public ValidatedBoolean stepUpSmoothingEnabled = new ValidatedBoolean(true);
        public ValidatedBoolean ignoreCrouchHeight = new ValidatedBoolean(true);
    }

    public static final class Aiming extends ConfigSection {
        public ValidatedBoolean disableWhileAiming = new ValidatedBoolean(true);
        public ValidatedDouble blendOutSpeed = new ValidatedDouble(16.0D, 60.0D, 1.0D);
        public ValidatedList<String> itemNamespaces = ValidatedList.ofString("irons_spellbooks", "epicfight");
        public ValidatedList<String> itemIds = ValidatedList.ofString();
    }

    public static final class Compatibility extends ConfigSection {
        public ValidatedBoolean pehkuiEnabled = new ValidatedBoolean(true);
    }

    public static final class Advanced extends ConfigSection {
        public ValidatedDouble firstPersonRotationFrequency = new ValidatedDouble(9.0D, 30.0D, 0.1D);
        public ValidatedDouble firstPersonRotationDamping = new ValidatedDouble(1.0D, 3.0D, 0.1D);
        public ValidatedDouble firstPersonVerticalResponse = new ValidatedDouble(14.0D, 80.0D, 1.0D);
        public ValidatedDouble firstPersonVerticalSnapThreshold = new ValidatedDouble(0.004D, 0.1D, 0.0D);
        public ValidatedDouble firstPersonVerticalFrequency = new ValidatedDouble(1.8D, 30.0D, 0.1D);
        public ValidatedDouble firstPersonVerticalDamping = new ValidatedDouble(1.05D, 3.0D, 0.1D);
        public ValidatedDouble thirdPersonPositionDamping = new ValidatedDouble(0.85D, 3.0D, 0.1D);
        public ValidatedDouble thirdPersonVerticalResponse = new ValidatedDouble(14.0D, 80.0D, 1.0D);
        public ValidatedDouble thirdPersonVerticalSnapThreshold = new ValidatedDouble(0.004D, 0.1D, 0.0D);
        public ValidatedDouble thirdPersonRotationFrequency = new ValidatedDouble(8.0D, 30.0D, 0.1D);
        public ValidatedDouble thirdPersonRotationDamping = new ValidatedDouble(1.0D, 3.0D, 0.1D);
        public ValidatedDouble thirdPersonStepUpMinHeight = new ValidatedDouble(0.05D, 0.5D, 0.0D);
        public ValidatedDouble thirdPersonStepUpMaxHeight = new ValidatedDouble(1.25D, 2.0D, 0.1D);
        public ValidatedDouble thirdPersonStepUpResponse = new ValidatedDouble(18.0D, 80.0D, 1.0D);
        public ValidatedDouble thirdPersonStepUpSnapThreshold = new ValidatedDouble(0.003D, 0.1D, 0.0D);
        public ValidatedDouble thirdPersonStepUpResetDistance = new ValidatedDouble(2.0D, 16.0D, 0.5D);
        public ValidatedDouble resetDistance = new ValidatedDouble(8.0D, 128.0D, 1.0D);
    }

    public static final class Experimental extends ConfigSection {
        public ValidatedBoolean thirdPersonRigEnabled = new ValidatedBoolean(false);
        public ValidatedDouble thirdPersonRigMinDistance = new ValidatedDouble(0.75D, 16.0D, 0.1D);
        public ValidatedDouble thirdPersonRigMaxDistance = new ValidatedDouble(6.0D, 32.0D, 0.5D);
        public ValidatedDouble thirdPersonRigCollisionPadding = new ValidatedDouble(0.08D, 0.5D, 0.0D);
    }
}

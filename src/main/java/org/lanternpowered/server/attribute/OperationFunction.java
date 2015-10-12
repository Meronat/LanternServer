package org.lanternpowered.server.attribute;

/**
 * Represents a function used by an {@link AttributeModifier} to modify the
 * value of an {@link Attribute}.
 */
@FunctionalInterface
public interface OperationFunction {

    /**
     * Gets the amount the {@link Attribute} should be incremented when this
     * operation function is applied to it.
     *
     * @param base The base value of the Attribute
     * @param modifier The modifier to modify the Attribute with
     * @param currentValue The current value of the Attribute
     * @return The amount the Attribute should be incremented when this modifier
     *         is applied to it
     */
    double getIncrementation(double base, double modifier, double currentValue);
}
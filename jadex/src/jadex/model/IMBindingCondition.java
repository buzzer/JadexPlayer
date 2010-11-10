package jadex.model;

/**
 *  A marker interface to distinguish binding conditions from other conditions.
 *  Binding conditions are affected by any binding options of parameters
 *  of the enclosing parameter element (e.g. a goal).
 *  A binding condition is evaluated for all available bindings.
 */
public interface IMBindingCondition extends IMCondition
{
}

package gg.norisk.enchantments.utils

import net.minecraft.client.render.entity.state.EntityRenderState
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * Utility class for copying EntityRenderState objects and their fields
 */
object RenderStateUtils {
    
    /**
     * Copies all fields from source EntityRenderState to target EntityRenderState
     * @param source The source EntityRenderState to copy from
     * @param target The target EntityRenderState to copy to
     */
    fun copyRenderState(source: EntityRenderState, target: EntityRenderState) {
        copyFields(source, target, EntityRenderState::class.java)
    }
    
    /**
     * Creates a copy of the given EntityRenderState
     * @param source The source EntityRenderState to copy
     * @return A new EntityRenderState with all fields copied from source
     */
    fun cloneRenderState(source: EntityRenderState): EntityRenderState {
        val target = EntityRenderState()
        copyRenderState(source, target)
        return target
    }
    
    /**
     * Copies all accessible fields from source to target object using reflection
     * @param source The source object to copy from
     * @param target The target object to copy to  
     * @param clazz The class type to get fields from
     */
    private fun copyFields(source: Any, target: Any, clazz: Class<*>) {
        // Get all declared fields including private ones
        val fields = clazz.declaredFields
        
        for (field in fields) {
            try {
                // Skip static and final fields
                if (Modifier.isStatic(field.modifiers) || Modifier.isFinal(field.modifiers)) {
                    continue
                }
                
                // Make field accessible
                field.isAccessible = true
                
                val value = field.get(source)
                
                // Handle different field types
                when {
                    value == null -> {
                        field.set(target, null)
                    }
                    
                    // Handle nested EntityRenderState classes
                    value is EntityRenderState.LeashData -> {
                        val targetLeashData = EntityRenderState.LeashData()
                        copyLeashData(value, targetLeashData)
                        field.set(target, targetLeashData)
                    }
                    
                    // Handle other nested objects that might need deep copying
                    field.type.name.startsWith("net.minecraft.client.render.entity.state") -> {
                        // For other EntityRenderState nested classes, try to copy recursively
                        try {
                            val nestedTarget = field.type.getDeclaredConstructor().newInstance()
                            copyFields(value, nestedTarget, field.type)
                            field.set(target, nestedTarget)
                        } catch (e: Exception) {
                            // If we can't instantiate, just set the reference
                            field.set(target, value)
                        }
                    }
                    
                    // For primitive types and immutable objects, direct assignment is fine
                    else -> {
                        field.set(target, value)
                    }
                }
                
            } catch (e: Exception) {
                // Log the error but continue with other fields
                println("Failed to copy field ${field.name}: ${e.message}")
            }
        }
        
        // Also copy fields from superclass if any
        val superClass = clazz.superclass
        if (superClass != null && superClass != Object::class.java) {
            copyFields(source, target, superClass)
        }
    }
    
    /**
     * Specialized method for copying LeashData fields
     */
    private fun copyLeashData(source: EntityRenderState.LeashData, target: EntityRenderState.LeashData) {
        target.offset = source.offset
        target.startPos = source.startPos
        target.endPos = source.endPos
        target.leashedEntityBlockLight = source.leashedEntityBlockLight
        target.leashHolderBlockLight = source.leashHolderBlockLight
        target.leashedEntitySkyLight = source.leashedEntitySkyLight
        target.leashHolderSkyLight = source.leashHolderSkyLight
    }
    
    /**
     * Helper method to get all fields including inherited ones
     */
    private fun getAllFields(clazz: Class<*>): List<Field> {
        val fields = mutableListOf<Field>()
        var currentClass: Class<*>? = clazz
        
        while (currentClass != null && currentClass != Object::class.java) {
            fields.addAll(currentClass.declaredFields)
            currentClass = currentClass.superclass
        }
        
        return fields
    }
} 
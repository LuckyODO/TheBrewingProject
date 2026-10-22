package dev.jsinco.brewery.recipes;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeRegistryImplTest {
    @Test
    void recipeGroupsRemainAnImmutableSnapshotAcrossRegistrationAndReload() {
        RecipeRegistryImpl<Object> registry = new RecipeRegistryImpl<>();
        RecipeGroupImpl<Object> beer = new RecipeGroupImpl<>("beer", Component.text("Beer"), List.of());
        RecipeGroupImpl<Object> tea = new RecipeGroupImpl<>("tea", Component.text("Tea"), List.of());
        registry.registerGroup(beer);
        var snapshot = registry.getRecipeGroups();

        registry.registerGroup(tea);
        assertEquals(List.of(beer, tea), List.copyOf(registry.getRecipeGroups()));
        assertEquals(List.of(beer), List.copyOf(snapshot));
        assertThrows(UnsupportedOperationException.class, snapshot::clear);

        registry.clear();
        assertTrue(registry.getRecipeGroups().isEmpty());
        assertEquals(List.of(beer), List.copyOf(snapshot));
    }
}

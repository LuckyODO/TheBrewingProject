package dev.jsinco.brewery.bukkit.structure;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.structure.StructureMeta;
import dev.jsinco.brewery.api.structure.StructureType;
import org.bukkit.block.BlockType;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StructureRegistry {

    private final Map<String, BreweryStructure> structureNames = new ConcurrentHashMap<>();
    private final Map<StructureType, Map<BlockType, Set<BreweryStructure>>> structuresWithMaterials = new ConcurrentHashMap<>();
    private final Map<StructureType, Set<BreweryStructure>> structures = new ConcurrentHashMap<>();

    public Optional<BreweryStructure> getStructure(@NonNull String key) {
        Preconditions.checkNotNull(key);
        return Optional.ofNullable(structureNames.get(key));
    }

    public Set<BreweryStructure> getPossibleStructures(@NonNull BlockType material, StructureType<?> structureType) {
        Preconditions.checkNotNull(material);
        Map<BlockType, Set<BreweryStructure>> byMaterial = structuresWithMaterials.get(structureType);
        if (byMaterial == null) {
            return Set.of();
        }
        return Set.copyOf(byMaterial.getOrDefault(material, Set.of()));
    }

    public void addStructure(@NonNull BreweryStructure structure) {
        Preconditions.checkNotNull(structure);
        structureNames.put(structure.getName(), structure);
        structures.computeIfAbsent(structure.getMeta(StructureMeta.TYPE), ignored -> ConcurrentHashMap.newKeySet()).add(structure);
        for (StructureMatcher structureMatcher : structure.getStructureMatchers()) {
            Set<BlockType> possibleMaterials = structureMatcher.dumpBlockTypes();
            Map<BlockType, Set<BreweryStructure>> materialStructureMap = structuresWithMaterials.computeIfAbsent(structure.getMeta(StructureMeta.TYPE), ignored -> new ConcurrentHashMap<>());
            possibleMaterials.forEach(material -> materialStructureMap.computeIfAbsent(material, ignored -> ConcurrentHashMap.newKeySet()).add(structure));
        }
    }

    public Collection<BreweryStructure> getStructures(StructureType structureType) {
        return Set.copyOf(structures.getOrDefault(structureType, Set.of()));
    }

    public void clear() {
        structures.clear();
        structureNames.clear();
        structuresWithMaterials.clear();
    }
}

package banding.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Genome {
    private List<Chromosome> chromosomes;

    public Chromosome getChromosome(String name) {
        return chromosomes.stream().filter(x -> x.getName().equals(name)).collect(Collectors.toList()).get(0);
    }

    public void addChromosome(Chromosome chromosome) {
        chromosomes.add(chromosome);
    }
}

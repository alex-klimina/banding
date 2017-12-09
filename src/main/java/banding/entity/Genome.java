package banding.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Data
public class Genome {
    private List<Chromosome> chromosomes;

    public Chromosome getChromosome(String name) {
        return chromosomes.stream().filter(x -> x.getName() == name).collect(Collectors.toList()).get(0);
    }
}

package banding.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Genome {
    private List<Chromosome> chromosomes = new ArrayList<>();

    public Chromosome getChromosome(String name) {
        return chromosomes.stream().filter(x -> x.getName().equals(name)).collect(Collectors.toList()).get(0);
    }

    public void addChromosome(Chromosome chromosome) {
        chromosomes.add(chromosome);
    }

    public long getLength() {
        long length = 0;
        for (Chromosome c: this.getChromosomes()) {
            length += c.getLength();
        }
        return length;
    }

    public long getCoverage() {
        long coverage = 0;
        for (Chromosome c: this.getChromosomes()) {
            coverage += c.getCoverage();
        }
        return coverage;
    }
}

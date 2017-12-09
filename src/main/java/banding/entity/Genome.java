package banding.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class Genome {
    private List<Chromosome> chromosomes;

}

package banding.entity;

import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
@Data
public class Chromosome {
    private String name;
    private Track track;
    private long start;
    private long end;

    public long getLength() {
        return end - start + 1;
    }

}

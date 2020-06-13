package arekkuusu.enderskills.api.capability.data;

public interface IInfoSorted extends Comparable<IInfoSorted> {

    String SORTED = "weight";

    void setWeight(int weight);

    int getWeight();

    @Override
    default int compareTo(IInfoSorted o) {
        return Integer.compare(o.getWeight(), getWeight());
    }
}

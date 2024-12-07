package handin2.Interfaces;

public interface WithOsmChoice {

    default void setChoice(String choice) {}
    default String getChoice() {
        return null;
    }
}
/**
 * Reference code for LoxClass.class file.
 */
class LoxClass {
    final String name;

    LoxClass(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof LoxClass) {
            return name.equals(((LoxClass) other).name);
        }

        return false;
    }
}

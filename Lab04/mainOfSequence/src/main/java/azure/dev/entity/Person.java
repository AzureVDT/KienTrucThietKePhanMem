package azure.dev.entity;

/**
 * @author VoDinhThong
 * @description Entity class representing a Person
 * @update 4/8/2024
 * @since 4/8/2024
 */
public abstract class Person {
    protected long id;
    protected String name;
    protected int age;
    protected String address;
    protected String email;
    protected String phone;

    public Person(long id, String name, int age, String address, String email, String phone) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.address = address;
        this.email = email;
        this.phone = phone;
    }

    public abstract long getId();
    public abstract String getName();
    public abstract int getAge();
    public abstract String getAddress();
    public abstract String getEmail();
    public abstract String getPhone();

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}

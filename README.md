Spring MVC laboratory work

# Stack
The following requirements must be considered when developing the IS:

  * Spring MVC must be used as the basis for the IS implementation.
  * JPA + Hibernate must be used to create the storage layer.
  * Different application layers must be separated from each other, and different logical parts of the IS must be in separate components.

## Task

Implement an information system that allows interaction with objects of the `Route` class, the description of which is provided below:

```java
public class Route {
    private Long id; // Field cannot be null, must be greater than 0, must be unique, must be generated automatically
    private String name; // Field cannot be null, cannot be empty
    private Coordinates coordinates; // Field cannot be null
    private java.util.Date creationDate; // Field cannot be null, must be generated automatically
    private Location from; // Field can be null
    private Location to; // Field cannot be null
    private int distance; // Field must be greater than 1
    private float rating; // Field must be greater than 0
}
public class Coordinates {
    private float x; // Maximum value: 421
    private double y; // Maximum value: 375
}
public class Location {
    private double x;
    private int y;
    private String name; // Field cannot be null
}
```

The developed system must meet the following requirements:

  * The main purpose of the information system is to manage objects created based on the class specified in the prompt.
  * The system must allow the following operations on objects: create a new object, get information about an object by ID, update an object (modify its attributes), and delete an object. These operations must be performed in separate application windows (interfaces). When getting information about a class object, information about its related objects must also be displayed.
  * When creating a class object, the user must be able to link the new object with auxiliary class objects that can be associated with the created object and are already in the system.
  * Object management operations must be performed on the server side (not on the client), and changes must be synchronized with the database.
  * The main screen of the system should display a list of current objects in a table format (each object attribute as a separate column). Pagination should be used when displaying the table if all objects don't fit on one screen.
  * The system must allow filtering/sorting table rows that display objects (by the values of any string column). Element filtering should only be done by exact match.
  * The option to update (modify) an object must be available from the table with the general list of objects and from the object visualization area (if implemented).
  * When an object is added, deleted, or changed, it should automatically appear, disappear, or change in the interfaces of other users authorized in the system.
  * If another object is linked to an object being deleted, the operation must be canceled, and the user must be informed that the object cannot be deleted.
  * Users must be able to view all objects. A separate dialog window must open for object modification. If incorrect values are entered into the object fields, informative error messages should appear.

The system must implement a separate user interface for performing special operations on objects:

  * Delete one (any) object whose `rating` field value is equivalent to a given value.
  * Return an array of objects whose `name` field value contains a given substring.
  * Return an array of objects whose `rating` field value is less than a given value.
  * Find the shortest (or longest) route between user-specified locations.
  * Add a new route between user-specified locations.

These operations must be implemented within the application's business logic components without direct use of database functions and procedures.

Object storage features that must be implemented in the system:

  * Organize data storage for objects in a relational DBMS (PostgreSQL). Every object that the IS works with must be saved in the database.
  * All requirements for class fields (specified as comments in the class descriptions) must be met at the ORM and DB level.
  * Use database tools to generate the `id` field.
  * To connect to the database on the departmental server, use the host `pg`, database name `studs`, and the same username/password as for the server connection.

When creating the system, the following features of user interaction organization must be considered:

  * The system must react to incorrect user input, restricting the entry of invalid values and informing users of the reason for the error.
  * Transitions between different logically separate parts of the system must be done using a menu.
  * When an object is added, deleted, or changed, it should automatically appear, disappear, or change in the area for all other clients.


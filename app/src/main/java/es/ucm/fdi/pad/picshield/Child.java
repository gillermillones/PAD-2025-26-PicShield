package es.ucm.fdi.pad.picshield;

public class Child {
    private String firstName;
    private String lastName;
    private String dni;
    private String photoUrl;
    private boolean allowPhotos;

    public Child() {} // necesario para Firestore

    public Child(String firstName, String lastName, String dni, String photoUrl, boolean allowPhotos) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dni = dni;
        this.photoUrl = photoUrl;
        this.allowPhotos = allowPhotos;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDni() { return dni; }
    public String getPhotoUrl() { return photoUrl; }
    public boolean isAllowPhotos() { return allowPhotos; }
}


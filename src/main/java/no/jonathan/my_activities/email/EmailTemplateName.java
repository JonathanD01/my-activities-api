package no.jonathan.my_activities.email;

public enum EmailTemplateName {

    ACTIVATE_ACCOUNT("activate_account");

    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }

    public String getTemplateName() {
        return this.name;
    }
}

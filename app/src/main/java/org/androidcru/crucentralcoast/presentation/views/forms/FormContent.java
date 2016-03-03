package org.androidcru.crucentralcoast.presentation.views.forms;

public interface FormContent
{
    void onNext();
    void onPrevious();
    void setupUI();
    void addDataObject(Object dataObject);
}
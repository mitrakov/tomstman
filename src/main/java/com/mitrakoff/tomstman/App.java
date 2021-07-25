package com.mitrakoff.tomstman;

import com.mitrakoff.tomstman.model.Model;
import com.mitrakoff.tomstman.view.View;
import com.mitrakoff.tomstman.view.Controller;

public class App {
    public static void main( String[] args ) {
        final Model model = new Model();
        final View view = new View();
        final Controller controller = new SimpleController(model);

        view.start(controller);
        model.setErrorHandler(view.getExceptionHandler());
    }
}

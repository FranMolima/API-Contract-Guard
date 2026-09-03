package io.github.franmolina.apicontractguard;

import java.util.List;
import java.util.Objects;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.OpenAPIV3Parser;

public class Main {

    public static void main(String[] args) {

        OpenAPIV3Parser parser = new OpenAPIV3Parser();

        // Leemos las dos especificaciones OpenAPI
        OpenAPI oldApi = parser.read("examples/old-api.yaml");
        OpenAPI newApi = parser.read("examples/new-api.yaml");

        if (oldApi != null && newApi != null) {

            Operation oldOperation = oldApi
                    .getPaths()
                    .get("/users")
                    .getGet();

            Operation newOperation = newApi
                    .getPaths()
                    .get("/users")
                    .getGet();

            List<Parameter> oldParameters = oldOperation.getParameters() != null
                    ? oldOperation.getParameters()
                    : List.of();
            List<Parameter> newParameters = newOperation.getParameters() != null
                    ? newOperation.getParameters()
                    : List.of();

            System.out.println("OpenAPI specifications parsed successfully!");

            System.out.println();
            System.out.println("============== OLD VERSION ==================");

            System.out.println("Title: " + oldApi.getInfo().getTitle());
            System.out.println("Version: " + oldApi.getInfo().getVersion());
            System.out.println("Paths: " + oldApi.getPaths().keySet());

            System.out.println();
            System.out.println("============== NEW VERSION ==================");

            System.out.println("Title: " + newApi.getInfo().getTitle());
            System.out.println("Version: " + newApi.getInfo().getVersion());
            System.out.println("Paths: " + newApi.getPaths().keySet());

            System.out.println();
            System.out.println("============== GET /users ==================");

            System.out.println("OLD summary: " + oldOperation.getSummary());
            System.out.println("NEW summary: " + newOperation.getSummary());

            if (oldParameters != null) {
                System.out.println();
                System.out.println("============== CHANGES ==================");

                // Buscamos el parámentro equivalente en la nueva versión y comparamos su estado de "required" + location
                for (Parameter parameter : oldParameters) {
                    Parameter newParam = newParameters.stream()
                            .filter(p -> Objects.equals(p.getName(), parameter.getName()) && Objects.equals(p.getIn(), parameter.getIn()))
                            .findFirst()
                            .orElse(null);

                    if (newParam != null) {

                        boolean oldRequired = Boolean.TRUE.equals(parameter.getRequired());
                        boolean newRequired = Boolean.TRUE.equals(newParam.getRequired());

                        if (!oldRequired && newRequired) {

                            System.out.println(
                                    "BREAKING CHANGE: parameter '"
                                            + parameter.getName()
                                            + "' became required."
                            );

                        } else {

                            System.out.println(
                                    "No breaking change detected in parameter '"
                                            + parameter.getName()
                                            + "' required status."
                            );
                        }
                    }else {
                        System.out.println(
                                "WARNING: parameter '"
                                        + parameter.getName()
                                        + "' ("
                                        + parameter.getIn()
                                        + ") was removed in the new version."
                        );
                    }
                }


            } else {
                System.out.println("No parameters found for GET /users.");
            }

        } else {
            System.out.println("Failed to parse OpenAPI specifications.");
        }
    }
}
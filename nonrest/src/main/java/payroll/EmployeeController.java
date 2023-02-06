package payroll;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
class EmployeeController {
    private final EmployeeRepository repository;

    private final EmployeeModelAssembler assembler;

    EmployeeController(EmployeeRepository repository,EmployeeModelAssembler assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    @DeleteMapping("/employees/{id}")
    ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        repository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/employees/{id}")
    ResponseEntity<?> replaceEmployees(@RequestBody Employee newEmployee, @PathVariable Long id) {
//        return repository.findById(id).map(employee -> {
//            employee.setName(newEmployee.getName());
//            employee.setRole(newEmployee.getRole());
//            return repository.save(employee);
//        }).orElseGet(() -> {
//            newEmployee.setId(id);
//            return repository.save(newEmployee);
//        });
        Employee updatedEmployee = repository.findById(id).map(employee -> {
            employee.setName(newEmployee.getName());
            employee.setRole(newEmployee.getRole());
            return repository.save(employee);
        }).orElseGet(()->{
            newEmployee.setId(id);
            return repository.save(newEmployee);
        });

        EntityModel<Employee> entityModel = assembler.toModel(updatedEmployee);

        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    // Single item
    @GetMapping("/employees/{id}")
    EntityModel<Employee> one(@PathVariable Long id) {
        Employee employee = repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));

//        return EntityModel.of(employee, //
//                linkTo(methodOn(EmployeeController.class).one(id)).withSelfRel(),
//                linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
//        return EntityModel.of(employee, linkTo(methodOn(EmployeeController.class).one(id)).withSelfRel(), linkTo(methodOn(EmployeeController.class).all()).withRel("employess"));
        return assembler.toModel(employee);
    }
//    Employee one(@PathVariable Long id) {
//        return repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
//    }

    @PostMapping("/employees")
    ResponseEntity<?> newEmployee(@RequestBody Employee newEmployee) {
//        return repository.save(newEmployee);
        EntityModel<Employee> entityModel = assembler.toModel(repository.save(newEmployee));

        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }


    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("/employees")
    CollectionModel<EntityModel<Employee>> all() {
//        List<EntityModel<Employee>> employees = repository.findAll().stream().map(employee -> EntityModel.of(employee, linkTo(methodOn(EmployeeController.class).one(employee.getId())).withSelfRel(), linkTo(methodOn(EmployeeController.class).all()).withRel("employees"))).collect(Collectors.toList());
        List<EntityModel<Employee>> employees = repository.findAll().stream().map(assembler::toModel).collect(Collectors.toList());

        return CollectionModel.of(employees, linkTo(methodOn(EmployeeController.class).all()).withSelfRel());

    }
//    List<Employee> all() {
//        return repository.findAll();
//    }
    // end::get-aggregate-root[]
}

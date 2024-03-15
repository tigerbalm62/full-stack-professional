package com.amigoscode.customer;

import com.amigoscode.exception.DuplicateResourceException;
import com.amigoscode.exception.RequestValidationException;
import com.amigoscode.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// no need to use AutoCloseable with the annotation @ExtendWith
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerDao customerDao;
    private CustomerService underTest;

    @BeforeEach
    void setUp() {
        //AutoCloseable autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new CustomerService(customerDao);

    }


    @Test
    void getAllCustomers() {
        // when
        underTest.getAllCustomers();
        // then
        verify(customerDao).selectAllCustomers();
    }

    @Test
    void canGetCustomer() {
        // given
        int id = 10;
        Customer customer = new Customer(
                id, "Alex", "alex@gmail", 19
        );
        // need to instruct the mock to return a value
        when(customerDao.selectCustomerById(id))
                .thenReturn(Optional.of(customer));
        // when
        Customer actual = underTest.getCustomer(id);
        // then
        assertThat(actual).isEqualTo(customer);
    }

    @Test
    void willThrowWhenGetCustomerReturnEmptyOptional() {
        // given
        int id = 10;

        // need to instruct the mock to return a value
        when(customerDao.selectCustomerById(id))
                .thenReturn(Optional.empty());
        // when

        // then
        assertThatThrownBy(() -> underTest.getCustomer(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("customer with id [%s] not found".formatted(id));
    }
    @Test
    void addCustomer() {
        // given
        String email = "alex@gmail";
        when(customerDao.existsCustomerWithEmail(email)).thenReturn(false);

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "Alex", email, 19
        );
        // when
        underTest.addCustomer(request);
        // then
        // need to capture the argument passed to the insertCustomer method - customer
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).insertCustomer(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();
        assertThat(capturedCustomer.getId()).isNull();
        assertThat(capturedCustomer.getName()).isEqualTo(request.name());
        assertThat(capturedCustomer.getEmail()).isEqualTo(request.email());
        assertThat(capturedCustomer.getAge()).isEqualTo(request.age());
    }

    @Test
    void willThrowWhenEmailExistsWhileAddingACustomer() {
        // given
        String email = "alex@gmail";
        when(customerDao.existsCustomerWithEmail(email)).thenReturn(true);

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "Alex", email, 19
        );
        // when

        assertThatThrownBy(() -> underTest.addCustomer(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("email already taken");
        // then
        verify(customerDao, Mockito.never()).insertCustomer(Mockito.any());
    }
    @Test
    void deleteCustomerById() {
        //given
        int id = 10;
        when(customerDao.existsCustomerById(id)).thenReturn(true);
        // when
        underTest.deleteCustomerById(id);
        // then
        verify(customerDao).deleteCustomerById(id);
    }

    @Test
    void willThrowWhenDeleteCustomerByIdNotExists() {
        //given
        int id = 10;
        when(customerDao.existsCustomerById(id)).thenReturn(false);
        // when
        assertThatThrownBy(() -> underTest.deleteCustomerById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("customer with id [%s] not found".formatted(id));
        // then
        verify(customerDao, Mockito.never()).deleteCustomerById(id);
    }

    @Test
    void canUpdateCustomerAllProperties() {
        // given
        int id = 10;
        Customer customer = new Customer(
                id, "Alex", "alex@gmail", 19
        );
        // need to instruct the mock to return a value
        when(customerDao.selectCustomerById(id))
                .thenReturn(Optional.of(customer));
        String newEmail = "alexandro@amigoscode.com";
        CustomerUpdateRequest req = new CustomerUpdateRequest(
                "Alexandro", newEmail, 23);
        when(customerDao.existsCustomerWithEmail(newEmail)).thenReturn(false);
        // when
        underTest.updateCustomer(id,req);
        // then
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(captor.capture());
        Customer captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(id);
        assertThat(captured.getName()).isEqualTo(req.name());
        assertThat(captured.getEmail()).isEqualTo(req.email());
        assertThat(captured.getAge()).isEqualTo(req.age());
    }

    @Test
    void canOnlyUpdateCustomerName() {
        // given
        int id = 10;
        Customer customer = new Customer(
                id, "Alex", "alex@gmail", 19
        );
        // need to instruct the mock to return a value
        when(customerDao.selectCustomerById(id))
                .thenReturn(Optional.of(customer));


        CustomerUpdateRequest req = new CustomerUpdateRequest(
                "Alexandro", null, null);

        // when
        underTest.updateCustomer(id,req);
        // then
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(captor.capture());
        Customer captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(id);
        assertThat(captured.getName()).isEqualTo(req.name());
        assertThat(captured.getEmail()).isEqualTo(customer.getEmail());
        assertThat(captured.getAge()).isEqualTo(customer.getAge());
    }

    @Test
    void canOnlyUpdateCustomerEmail() {
        // given
        int id = 10;
        Customer customer = new Customer(
                id, "Alex", "alex@gmail", 19
        );
        // need to instruct the mock to return a value
        when(customerDao.selectCustomerById(id))
                .thenReturn(Optional.of(customer));

        String newEmail = "alexandro@amigoscode.com";
        CustomerUpdateRequest req = new CustomerUpdateRequest(
                null, newEmail, null);
        when(customerDao.existsCustomerWithEmail(newEmail)).thenReturn(false);
        // when
        underTest.updateCustomer(id,req);
        // then
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(captor.capture());
        Customer captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(id);
        assertThat(captured.getName()).isEqualTo(customer.getName());
        assertThat(captured.getEmail()).isEqualTo(newEmail);
        assertThat(captured.getAge()).isEqualTo(customer.getAge());
    }
    @Test
    void canOnlyUpdateCustomerAge() {
        // given
        int id = 10;
        Customer customer = new Customer(
                id, "Alex", "alex@gmail", 19
        );
        // need to instruct the mock to return a value
        when(customerDao.selectCustomerById(id))
                .thenReturn(Optional.of(customer));

        Integer newAge = 32;
        CustomerUpdateRequest req = new CustomerUpdateRequest(
                null, null, newAge);
        //when(customerDao.existsCustomerWithEmail(newEmail)).thenReturn(false);
        // when
        underTest.updateCustomer(id,req);
        // then
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(captor.capture());
        Customer captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(id);
        assertThat(captured.getName()).isEqualTo(customer.getName());
        assertThat(captured.getEmail()).isEqualTo(customer.getEmail());
        assertThat(captured.getAge()).isEqualTo(req.age());
    }

    @Test
    void willThrowWhenUpdateCustomerEmailAlreadyTaken() {
        // given
        int id = 10;
        Customer customer = new Customer(
                id, "Alex", "alex@gmail", 19
        );
        // need to instruct the mock to return a value
        when(customerDao.selectCustomerById(id))
                .thenReturn(Optional.of(customer));

        String newEmail = "alexandro@amigoscode.com";
        CustomerUpdateRequest req = new CustomerUpdateRequest(
                null, newEmail, null);
        when(customerDao.existsCustomerWithEmail(newEmail)).thenReturn(true);
        // when
        assertThatThrownBy(() -> underTest.updateCustomer(id, req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("email already taken");
        verify(customerDao, Mockito.never()).updateCustomer(Mockito.any());
    }

    @Test
    void willThrowWhenCustomerUpdateHasNoChanges() {
        // given
        int id = 10;
        Customer customer = new Customer(
                id, "Alex", "alex@gmail.com", 19
        );
        // need to instruct the mock to return a value
        when(customerDao.selectCustomerById(id))
                .thenReturn(Optional.of(customer));
        String newEmail = "alexandro@amigoscode.com";
        CustomerUpdateRequest req = new CustomerUpdateRequest(
                customer.getName(), customer.getEmail(), customer.getAge());

        //when(customerDao.existsCustomerWithEmail(newEmail)).thenReturn(false);
        // when

        // then
        assertThatThrownBy(() -> underTest.updateCustomer(id, req))
                .isInstanceOf(RequestValidationException.class)
                .hasMessage("no data changes found");
        verify(customerDao, Mockito.never()).updateCustomer(Mockito.any());
    }
}
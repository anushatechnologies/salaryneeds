package com.salaryneeds.repository;

import com.salaryneeds.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findByCustomerId(UUID customerId);

    Optional<Address> findByIdAndCustomerId(UUID addressId, UUID customerId);

    List<Address> findByCustomerIdAndIsDefaultTrue(UUID customerId);

}

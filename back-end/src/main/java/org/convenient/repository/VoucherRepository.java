package org.convenient.repository;

import org.convenient.models.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, String> {
    Optional<Voucher> findByVccode(String vccode);
}

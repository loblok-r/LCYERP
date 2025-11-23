package cn.loblok.erpcore.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "company_bank_config", schema = "erp_test", catalog = "")
public class CompanyBankConfig {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "company_id")
    private long companyId;
    @Basic
    @Column(name = "bank_code")
    private String bankCode;

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyBankConfig that = (CompanyBankConfig) o;
        return companyId == that.companyId && Objects.equals(bankCode, that.bankCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyId, bankCode);
    }
}
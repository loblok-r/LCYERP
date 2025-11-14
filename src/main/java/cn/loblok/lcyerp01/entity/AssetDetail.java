package cn.loblok.lcyerp01.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Data
@Table(name = "asset_detail", schema = "erp_test", catalog = "")
public class AssetDetail {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private long id;
    @Basic
    @Column(name = "asset_code")
    private String assetCode;
    @Basic
    @Column(name = "asset_name")
    private String assetName;
    @Basic
    @Column(name = "category_code")
    private String categoryCode;
    @Basic
    @Column(name = "category_name")
    private String categoryName;
    @Basic
    @Column(name = "company_id")
    private int companyId;
    @Basic
    @Column(name = "company_name")
    private String companyName;
    @Basic
    @Column(name = "dept_id")
    private int deptId;
    @Basic
    @Column(name = "dept_name")
    private String deptName;
    @Basic
    @Column(name = "original_value")
    private BigDecimal originalValue;
    @Basic
    @Column(name = "residual_value")
    private BigDecimal residualValue;
    @Basic
    @Column(name = "depreciation_method")
    private byte depreciationMethod;
    @Basic
    @Column(name = "accumulated_depreciation")
    private BigDecimal accumulatedDepreciation;
    @Basic
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
    @Basic
    @Column(name = "use_start_date")
    private LocalDate  useStartDate;
    @Basic
    @Column(name = "expected_life_months")
    private int expectedLifeMonths;
    @Basic
    @Column(name = "status")
    private byte status;
    @Basic
    @Column(name = "location")
    private String location;
    @Basic
    @Column(name = "custodian")
    private String custodian;
    @Basic
    @Column(name = "supplier_name")
    private String supplierName;
    @Basic
    @Column(name = "contract_no")
    private String contractNo;
    @Basic
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Basic
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetDetail that = (AssetDetail) o;
        return id == that.id && companyId == that.companyId && deptId == that.deptId && depreciationMethod == that.depreciationMethod && expectedLifeMonths == that.expectedLifeMonths && status == that.status && Objects.equals(assetCode, that.assetCode) && Objects.equals(assetName, that.assetName) && Objects.equals(categoryCode, that.categoryCode) && Objects.equals(categoryName, that.categoryName) && Objects.equals(companyName, that.companyName) && Objects.equals(deptName, that.deptName) && Objects.equals(originalValue, that.originalValue) && Objects.equals(residualValue, that.residualValue) && Objects.equals(accumulatedDepreciation, that.accumulatedDepreciation) && Objects.equals(purchaseDate, that.purchaseDate) && Objects.equals(useStartDate, that.useStartDate) && Objects.equals(location, that.location) && Objects.equals(custodian, that.custodian) && Objects.equals(supplierName, that.supplierName) && Objects.equals(contractNo, that.contractNo) && Objects.equals(createdAt, that.createdAt) && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, assetCode, assetName, categoryCode, categoryName, companyId, companyName, deptId, deptName, originalValue, residualValue, depreciationMethod, accumulatedDepreciation, purchaseDate, useStartDate, expectedLifeMonths, status, location, custodian, supplierName, contractNo, createdAt, updatedAt);
    }
}
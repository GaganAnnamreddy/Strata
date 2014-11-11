/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.platform.finance.equity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.DerivedProperty;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.ImmutableMap;
import com.opengamma.basics.currency.CurrencyAmount;
import com.opengamma.platform.finance.Trade;
import com.opengamma.platform.finance.TradeType;
import com.opengamma.platform.source.id.StandardId;
import com.opengamma.platform.source.link.Link;

/**
 * A trade representing the purchase or sale of an equity.
 * <p>
 * A trade in an underlying {@link Equity}.
 * For example, the purchase of 1000 shares of IBM.
 */
@BeanDefinition
public final class EquityTrade
    implements Trade, ImmutableBean, Serializable {

  /**
   * The trade type constant for this class - 'Equity'.
   */
  public static final TradeType TYPE = TradeType.of("Equity");

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The primary standard identifier for the trade.
   * <p>
   * The standard identifier is used to identify the trade.
   * It will typically be an identifier in an external data system.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final StandardId standardId;
  /**
   * The trade date.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final LocalDate tradeDate;
  /**
   * The general purpose trade attributes.
   * Most data in the trade is available as bean properties.
   * Attributes are used to tag the object with additional information.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final ImmutableMap<String, String> attributes;

  /**
   * The link referencing the equity.
   */
  @PropertyDefinition(validate = "notNull")
  private final Link<Equity> _equityLink;
  /**
   * The number of units of the equity in the trade.
   * This will be positive if buying and negative if selling.
   */
  @PropertyDefinition
  private final double quantity;
  /**
   * Amount paid for the equity at time of purchase, optional.
   * This will be positive if buying and negative if selling.
   * If the amount is not known, it is set to null.
   */
  @PropertyDefinition
  private final CurrencyAmount payment;

  //-------------------------------------------------------------------------
  /**
   * Gets the trade type.
   * 
   * @return {@link #TYPE}
   */
  @Override
  @DerivedProperty
  public TradeType getTradeType() {
    return TYPE;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code EquityTrade}.
   * @return the meta-bean, not null
   */
  public static EquityTrade.Meta meta() {
    return EquityTrade.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(EquityTrade.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static EquityTrade.Builder builder() {
    return new EquityTrade.Builder();
  }

  private EquityTrade(
      StandardId standardId,
      LocalDate tradeDate,
      Map<String, String> attributes,
      Link<Equity> _equityLink,
      double quantity,
      CurrencyAmount payment) {
    JodaBeanUtils.notNull(standardId, "standardId");
    JodaBeanUtils.notNull(tradeDate, "tradeDate");
    JodaBeanUtils.notNull(attributes, "attributes");
    JodaBeanUtils.notNull(_equityLink, "_equityLink");
    this.standardId = standardId;
    this.tradeDate = tradeDate;
    this.attributes = ImmutableMap.copyOf(attributes);
    this._equityLink = _equityLink;
    this.quantity = quantity;
    this.payment = payment;
  }

  @Override
  public EquityTrade.Meta metaBean() {
    return EquityTrade.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the primary standard identifier for the trade.
   * <p>
   * The standard identifier is used to identify the trade.
   * It will typically be an identifier in an external data system.
   * @return the value of the property, not null
   */
  @Override
  public StandardId getStandardId() {
    return standardId;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the trade date.
   * @return the value of the property, not null
   */
  @Override
  public LocalDate getTradeDate() {
    return tradeDate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the general purpose trade attributes.
   * Most data in the trade is available as bean properties.
   * Attributes are used to tag the object with additional information.
   * @return the value of the property, not null
   */
  @Override
  public ImmutableMap<String, String> getAttributes() {
    return attributes;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the link referencing the equity.
   * @return the value of the property, not null
   */
  public Link<Equity> get_equityLink() {
    return _equityLink;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the number of units of the equity in the trade.
   * This will be positive if buying and negative if selling.
   * @return the value of the property
   */
  public double getQuantity() {
    return quantity;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets amount paid for the equity at time of purchase, optional.
   * This will be positive if buying and negative if selling.
   * If the amount is not known, it is set to null.
   * @return the value of the property
   */
  public CurrencyAmount getPayment() {
    return payment;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      EquityTrade other = (EquityTrade) obj;
      return JodaBeanUtils.equal(getStandardId(), other.getStandardId()) &&
          JodaBeanUtils.equal(getTradeDate(), other.getTradeDate()) &&
          JodaBeanUtils.equal(getAttributes(), other.getAttributes()) &&
          JodaBeanUtils.equal(get_equityLink(), other.get_equityLink()) &&
          JodaBeanUtils.equal(getQuantity(), other.getQuantity()) &&
          JodaBeanUtils.equal(getPayment(), other.getPayment()) &&
          JodaBeanUtils.equal(getTradeType(), other.getTradeType());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getStandardId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTradeDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAttributes());
    hash += hash * 31 + JodaBeanUtils.hashCode(get_equityLink());
    hash += hash * 31 + JodaBeanUtils.hashCode(getQuantity());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPayment());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTradeType());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("EquityTrade{");
    buf.append("standardId").append('=').append(getStandardId()).append(',').append(' ');
    buf.append("tradeDate").append('=').append(getTradeDate()).append(',').append(' ');
    buf.append("attributes").append('=').append(getAttributes()).append(',').append(' ');
    buf.append("_equityLink").append('=').append(get_equityLink()).append(',').append(' ');
    buf.append("quantity").append('=').append(getQuantity()).append(',').append(' ');
    buf.append("payment").append('=').append(getPayment()).append(',').append(' ');
    buf.append("tradeType").append('=').append(JodaBeanUtils.toString(getTradeType()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code EquityTrade}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code standardId} property.
     */
    private final MetaProperty<StandardId> standardId = DirectMetaProperty.ofImmutable(
        this, "standardId", EquityTrade.class, StandardId.class);
    /**
     * The meta-property for the {@code tradeDate} property.
     */
    private final MetaProperty<LocalDate> tradeDate = DirectMetaProperty.ofImmutable(
        this, "tradeDate", EquityTrade.class, LocalDate.class);
    /**
     * The meta-property for the {@code attributes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableMap<String, String>> attributes = DirectMetaProperty.ofImmutable(
        this, "attributes", EquityTrade.class, (Class) ImmutableMap.class);
    /**
     * The meta-property for the {@code _equityLink} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Link<Equity>> _equityLink = DirectMetaProperty.ofImmutable(
        this, "_equityLink", EquityTrade.class, (Class) Link.class);
    /**
     * The meta-property for the {@code quantity} property.
     */
    private final MetaProperty<Double> quantity = DirectMetaProperty.ofImmutable(
        this, "quantity", EquityTrade.class, Double.TYPE);
    /**
     * The meta-property for the {@code payment} property.
     */
    private final MetaProperty<CurrencyAmount> payment = DirectMetaProperty.ofImmutable(
        this, "payment", EquityTrade.class, CurrencyAmount.class);
    /**
     * The meta-property for the {@code tradeType} property.
     */
    private final MetaProperty<TradeType> tradeType = DirectMetaProperty.ofDerived(
        this, "tradeType", EquityTrade.class, TradeType.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "standardId",
        "tradeDate",
        "attributes",
        "_equityLink",
        "quantity",
        "payment",
        "tradeType");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1284477768:  // standardId
          return standardId;
        case 752419634:  // tradeDate
          return tradeDate;
        case 405645655:  // attributes
          return attributes;
        case 220474878:  // _equityLink
          return _equityLink;
        case -1285004149:  // quantity
          return quantity;
        case -786681338:  // payment
          return payment;
        case 752919230:  // tradeType
          return tradeType;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public EquityTrade.Builder builder() {
      return new EquityTrade.Builder();
    }

    @Override
    public Class<? extends EquityTrade> beanType() {
      return EquityTrade.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code standardId} property.
     * @return the meta-property, not null
     */
    public MetaProperty<StandardId> standardId() {
      return standardId;
    }

    /**
     * The meta-property for the {@code tradeDate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> tradeDate() {
      return tradeDate;
    }

    /**
     * The meta-property for the {@code attributes} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ImmutableMap<String, String>> attributes() {
      return attributes;
    }

    /**
     * The meta-property for the {@code _equityLink} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Link<Equity>> _equityLink() {
      return _equityLink;
    }

    /**
     * The meta-property for the {@code quantity} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> quantity() {
      return quantity;
    }

    /**
     * The meta-property for the {@code payment} property.
     * @return the meta-property, not null
     */
    public MetaProperty<CurrencyAmount> payment() {
      return payment;
    }

    /**
     * The meta-property for the {@code tradeType} property.
     * @return the meta-property, not null
     */
    public MetaProperty<TradeType> tradeType() {
      return tradeType;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1284477768:  // standardId
          return ((EquityTrade) bean).getStandardId();
        case 752419634:  // tradeDate
          return ((EquityTrade) bean).getTradeDate();
        case 405645655:  // attributes
          return ((EquityTrade) bean).getAttributes();
        case 220474878:  // _equityLink
          return ((EquityTrade) bean).get_equityLink();
        case -1285004149:  // quantity
          return ((EquityTrade) bean).getQuantity();
        case -786681338:  // payment
          return ((EquityTrade) bean).getPayment();
        case 752919230:  // tradeType
          return ((EquityTrade) bean).getTradeType();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code EquityTrade}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<EquityTrade> {

    private StandardId standardId;
    private LocalDate tradeDate;
    private Map<String, String> attributes = new HashMap<String, String>();
    private Link<Equity> _equityLink;
    private double quantity;
    private CurrencyAmount payment;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(EquityTrade beanToCopy) {
      this.standardId = beanToCopy.getStandardId();
      this.tradeDate = beanToCopy.getTradeDate();
      this.attributes = new HashMap<String, String>(beanToCopy.getAttributes());
      this._equityLink = beanToCopy.get_equityLink();
      this.quantity = beanToCopy.getQuantity();
      this.payment = beanToCopy.getPayment();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1284477768:  // standardId
          return standardId;
        case 752419634:  // tradeDate
          return tradeDate;
        case 405645655:  // attributes
          return attributes;
        case 220474878:  // _equityLink
          return _equityLink;
        case -1285004149:  // quantity
          return quantity;
        case -786681338:  // payment
          return payment;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1284477768:  // standardId
          this.standardId = (StandardId) newValue;
          break;
        case 752419634:  // tradeDate
          this.tradeDate = (LocalDate) newValue;
          break;
        case 405645655:  // attributes
          this.attributes = (Map<String, String>) newValue;
          break;
        case 220474878:  // _equityLink
          this._equityLink = (Link<Equity>) newValue;
          break;
        case -1285004149:  // quantity
          this.quantity = (Double) newValue;
          break;
        case -786681338:  // payment
          this.payment = (CurrencyAmount) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public EquityTrade build() {
      return new EquityTrade(
          standardId,
          tradeDate,
          attributes,
          _equityLink,
          quantity,
          payment);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code standardId} property in the builder.
     * @param standardId  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder standardId(StandardId standardId) {
      JodaBeanUtils.notNull(standardId, "standardId");
      this.standardId = standardId;
      return this;
    }

    /**
     * Sets the {@code tradeDate} property in the builder.
     * @param tradeDate  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder tradeDate(LocalDate tradeDate) {
      JodaBeanUtils.notNull(tradeDate, "tradeDate");
      this.tradeDate = tradeDate;
      return this;
    }

    /**
     * Sets the {@code attributes} property in the builder.
     * @param attributes  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder attributes(Map<String, String> attributes) {
      JodaBeanUtils.notNull(attributes, "attributes");
      this.attributes = attributes;
      return this;
    }

    /**
     * Sets the {@code _equityLink} property in the builder.
     * @param _equityLink  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder _equityLink(Link<Equity> _equityLink) {
      JodaBeanUtils.notNull(_equityLink, "_equityLink");
      this._equityLink = _equityLink;
      return this;
    }

    /**
     * Sets the {@code quantity} property in the builder.
     * @param quantity  the new value
     * @return this, for chaining, not null
     */
    public Builder quantity(double quantity) {
      this.quantity = quantity;
      return this;
    }

    /**
     * Sets the {@code payment} property in the builder.
     * @param payment  the new value
     * @return this, for chaining, not null
     */
    public Builder payment(CurrencyAmount payment) {
      this.payment = payment;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(224);
      buf.append("EquityTrade.Builder{");
      buf.append("standardId").append('=').append(JodaBeanUtils.toString(standardId)).append(',').append(' ');
      buf.append("tradeDate").append('=').append(JodaBeanUtils.toString(tradeDate)).append(',').append(' ');
      buf.append("attributes").append('=').append(JodaBeanUtils.toString(attributes)).append(',').append(' ');
      buf.append("_equityLink").append('=').append(JodaBeanUtils.toString(_equityLink)).append(',').append(' ');
      buf.append("quantity").append('=').append(JodaBeanUtils.toString(quantity)).append(',').append(' ');
      buf.append("payment").append('=').append(JodaBeanUtils.toString(payment));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
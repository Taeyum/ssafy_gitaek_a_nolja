package com.ssafy.exam.model.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TripPlan implements Serializable {
    private static final long serialVersionUID = 1L;

    private int planId;
    private String title;
    private String startDate;
    private String endDate;
    private String budget;
    private String memo;
    private String createdAt;
    private String updatedAt;
    private List<PlanItem> items = new ArrayList<>();

    public TripPlan() {
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<PlanItem> getItems() {
        return items;
    }

    public void setItems(List<PlanItem> items) {
        this.items = (items == null) ? new ArrayList<>() : new ArrayList<>(items);
    }

    public void addItem(PlanItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        if (item != null) {
            items.add(item);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlanItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer planItemId;
        private String localId;
        private Integer orderIndex;
        private String placeId;
        private String title;
        private String address;
        private String day;
        private String startTime;
        private String endTime;
        private String memo;
        private Double mapx;
        private Double mapy;

        public PlanItem() {
        }

        public Integer getPlanItemId() {
            return planItemId;
        }

        public void setPlanItemId(Integer planItemId) {
            this.planItemId = planItemId;
        }

        public String getLocalId() {
            return localId;
        }

        public void setLocalId(String localId) {
            this.localId = localId;
        }

        public Integer getOrderIndex() {
            return orderIndex;
        }

        public void setOrderIndex(Integer orderIndex) {
            this.orderIndex = orderIndex;
        }

        public String getPlaceId() {
            return placeId;
        }

        public void setPlaceId(String placeId) {
            this.placeId = placeId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getMemo() {
            return memo;
        }

        public void setMemo(String memo) {
            this.memo = memo;
        }

        public Double getMapx() {
            return mapx;
        }

        public void setMapx(Double mapx) {
            this.mapx = mapx;
        }

        public Double getMapy() {
            return mapy;
        }

        public void setMapy(Double mapy) {
            this.mapy = mapy;
        }

        public LocalDate parseVisitDay() {
            if (day == null || day.isBlank()) {
                return null;
            }
            return LocalDate.parse(day);
        }

        @Override
        public String toString() {
            return "PlanItem{" +
                    "planItemId=" + planItemId +
                    ", localId='" + localId + '\'' +
                    ", orderIndex=" + orderIndex +
                    ", placeId='" + placeId + '\'' +
                    ", title='" + title + '\'' +
                    ", address='" + address + '\'' +
                    ", day='" + day + '\'' +
                    ", startTime='" + startTime + '\'' +
                    ", endTime='" + endTime + '\'' +
                    ", memo='" + memo + '\'' +
                    ", mapx=" + mapx +
                    ", mapy=" + mapy +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PlanItem planItem)) return false;
            return Objects.equals(localId, planItem.localId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(localId);
        }
    }
}
